package showcase.api;

import com.github.benmanes.caffeine.cache.AsyncCache;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import one.util.streamex.StreamEx;
import org.apache.commons.lang3.function.Predicates;
import org.axonframework.common.AxonException;
import org.axonframework.common.IdentifierFactory;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException.Visitor;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.netty.channel.AbortedException;
import showcase.command.FinishShowcaseCommand;
import showcase.command.RemoveShowcaseCommand;
import showcase.command.ScheduleShowcaseCommand;
import showcase.command.ShowcaseCommandException;
import showcase.command.ShowcaseCommandOperations;
import showcase.command.StartShowcaseCommand;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryException;
import showcase.query.ShowcaseQueryOperations;
import showcase.query.ShowcaseStatus;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/showcases")
@RequiredArgsConstructor
@Slf4j
final class ShowcaseApiController implements ShowcaseApi {

    private final ShowcaseCommandOperations commandOperations;

    private final ShowcaseQueryOperations queryOperations;

    private final AsyncCache<FetchShowcaseListQuery, List<String>> fetchShowcaseListCache;

    private final AsyncCache<String, Showcase> fetchShowcaseByIdCache;

    private final MessageSource messageSource;

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    @Override
    public Mono<ResponseEntity<ScheduleShowcaseResponse>> schedule(
            @RequestHeader(name = IDEMPOTENCY_KEY_HEADER, required = false) String idempotencyKey,
            @RequestBody ScheduleShowcaseRequest request) {
        return Mono.justOrEmpty(idempotencyKey)
                   .switchIfEmpty(Mono.fromSupplier(() -> IdentifierFactory.getInstance().generateIdentifier())
                                      .subscribeOn(Schedulers.boundedElastic()))
                   .flatMap(showcaseId ->
                                    commandOperations
                                            .schedule(ScheduleShowcaseCommand
                                                              .builder()
                                                              .showcaseId(showcaseId)
                                                              .title(request.title())
                                                              .startTime(request.startTime())
                                                              .duration(request.duration())
                                                              .build())
                                            .thenReturn(
                                                    ResponseEntity
                                                            .created(fromUriString("/showcases/")
                                                                             .path(showcaseId)
                                                                             .build()
                                                                             .toUri())
                                                            .body(ScheduleShowcaseResponse
                                                                          .builder()
                                                                          .showcaseId(showcaseId)
                                                                          .build()))
                                            .onErrorReturn(
                                                    TimeoutException.class,
                                                    ResponseEntity
                                                            .accepted()
                                                            .header(IDEMPOTENCY_KEY_HEADER, showcaseId)
                                                            .build()));
    }

    @PutMapping("/{showcaseId}/start")
    @Override
    public Mono<ResponseEntity<Void>> start(@PathVariable String showcaseId) {
        return commandOperations
                       .start(StartShowcaseCommand
                                      .builder()
                                      .showcaseId(showcaseId)
                                      .build())
                       .thenReturn(HttpStatus.OK)
                       .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                       .map(status -> ResponseEntity.status(status).build());
    }

    @PutMapping("/{showcaseId}/finish")
    @Override
    public Mono<ResponseEntity<Void>> finish(@PathVariable String showcaseId) {
        return commandOperations
                       .finish(FinishShowcaseCommand
                                       .builder()
                                       .showcaseId(showcaseId)
                                       .build())
                       .thenReturn(HttpStatus.OK)
                       .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                       .map(status -> ResponseEntity.status(status).build());
    }

    @DeleteMapping("/{showcaseId}")
    @Override
    public Mono<ResponseEntity<Void>> remove(@PathVariable String showcaseId) {
        return commandOperations
                       .remove(RemoveShowcaseCommand
                                       .builder()
                                       .showcaseId(showcaseId)
                                       .build())
                       .thenReturn(HttpStatus.OK)
                       .onErrorReturn(TimeoutException.class, HttpStatus.ACCEPTED)
                       .map(status -> ResponseEntity.status(status).build());
    }

    @GetMapping
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public Flux<Showcase> fetchList(
            @RequestParam(required = false) String title,
            @RequestParam(name = "status", required = false) List<ShowcaseStatus> statuses,
            @RequestParam(required = false) String afterId,
            @RequestParam(required = false, defaultValue = "" + FetchShowcaseListQuery.DEFAULT_SIZE) int size) {
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .title(title)
                        .statuses(statuses)
                        .afterId(afterId)
                        .size(size)
                        .build();
        return queryOperations
                       .fetchList(query)
                       .doOnNext(showcase -> fetchShowcaseByIdCache.put(
                               showcase.showcaseId(), completedFuture(showcase)))
                       .collectList()
                       .doOnNext(showcases -> fetchShowcaseListCache.put(
                               query, completedFuture(showcases.stream()
                                                               .map(Showcase::showcaseId)
                                                               .toList())))
                       .flatMapIterable(Function.identity())
                       .onErrorResume(
                               Predicate.not(ShowcaseQueryException.class::isInstance),
                               t -> Flux.<String>create(sink -> {
                                            val future = fetchShowcaseListCache.getIfPresent(query);
                                            if (future != null) {
                                                future.whenComplete((showcaseIds, t2) -> {
                                                    if (t2 == null) {
                                                        showcaseIds.forEach(sink::next);
                                                        sink.complete();
                                                    } else {
                                                        sink.error(t);
                                                    }
                                                });
                                            } else {
                                                sink.error(t);
                                            }
                                        })
                                        .<Showcase>handle((showcaseId, sink) -> {
                                            val future = fetchShowcaseByIdCache.getIfPresent(showcaseId);
                                            if (future != null) {
                                                future.whenComplete((showcase, t2) -> {
                                                    if (t2 == null) {
                                                        sink.next(showcase);
                                                    } else {
                                                        sink.error(t);
                                                    }
                                                });
                                            } else {
                                                sink.error(t);
                                            }
                                        })
                                        .doOnComplete(() -> log.warn("Fallback on {}", query, t)));
    }

    @GetMapping("/{showcaseId}")
    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public Mono<Showcase> fetchById(@PathVariable String showcaseId) {
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        return queryOperations
                       .fetchById(query)
                       .doOnNext(showcase -> fetchShowcaseByIdCache.put(showcaseId, completedFuture(showcase)))
                       .onErrorResume(
                               Predicate.not(ShowcaseQueryException.class::isInstance),
                               t -> Mono.<Showcase>create(sink -> {
                                            val future = fetchShowcaseByIdCache.getIfPresent(showcaseId);
                                            if (future != null) {
                                                future.whenComplete((showcase, t2) -> {
                                                    if (t2 == null) {
                                                        sink.success(showcase);
                                                    } else {
                                                        sink.error(t);
                                                    }
                                                });
                                            } else {
                                                sink.error(t);
                                            }
                                        })
                                        .doOnSuccess(__ -> log.warn("Fallback on {}", query, t)));
    }

    @ExceptionHandler
    private ProblemDetail handleShowcaseCommandException(ShowcaseCommandException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail = switch (errorDetails.errorCode()) {
            case INVALID_COMMAND -> {
                val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.errorMessage());
                pd.setProperty("fieldErrors", errorDetails.metaData());
                yield pd;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.errorMessage());
            case TITLE_IN_USE, ILLEGAL_STATE -> ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, errorDetails.errorMessage());
        };
        problemDetail.setProperty("code", errorDetails.errorCode());
        return problemDetail;
    }

    @ExceptionHandler
    private ProblemDetail handleShowcaseQueryException(ShowcaseQueryException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail = switch (errorDetails.errorCode()) {
            case INVALID_QUERY -> {
                val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.errorMessage());
                pd.setProperty("fieldErrors", errorDetails.metaData());
                yield pd;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.errorMessage());
        };
        problemDetail.setProperty("code", errorDetails.errorCode());
        return problemDetail;
    }

    @ExceptionHandler
    private ProblemDetail handleHandlerMethodValidationException(
            HandlerMethodValidationException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request.");

        val cookieErrors = new LinkedHashMap<String, List<String>>();
        val modelErrors = new LinkedHashMap<String, Map<String, List<String>>>();
        val pathErrors = new LinkedHashMap<String, List<String>>();
        val bodyErrors = new LinkedHashMap<String, List<String>>();
        val headerErrors = new LinkedHashMap<String, List<String>>();
        val paramErrors = new LinkedHashMap<String, List<String>>();
        val partErrors = new LinkedHashMap<String, Map<String, List<String>>>();
        val otherErrors = new LinkedHashMap<String, List<String>>();

        e.visitResults(new Visitor() {

            @Override
            public void cookieValue(CookieValue cookieValue, ParameterValidationResult result) {
                cookieErrors.put(
                        Optional.of(cookieValue.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(result.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve cookie name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void matrixVariable(MatrixVariable matrixVariable, ParameterValidationResult result) {
                pathErrors.put(
                        Optional.of(matrixVariable.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(result.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve matrix variable name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void modelAttribute(@Nullable ModelAttribute modelAttribute, ParameterErrors errors) {
                modelErrors.put(
                        Optional.ofNullable(modelAttribute)
                                .map(ModelAttribute::name)
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(errors.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve model attribute name for %s"
                                                .formatted(errors.getMethodParameter()))),
                        StreamEx.of(errors.getFieldErrors())
                                .mapToEntry(
                                        FieldError::getField,
                                        fieldError -> messageSource.getMessage(fieldError, locale))
                                .collapseKeys()
                                .toMap());
            }

            @Override
            public void pathVariable(PathVariable pathVariable, ParameterValidationResult result) {
                pathErrors.put(
                        Optional.of(pathVariable.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(result.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve path variable name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestBody(RequestBody requestBody, ParameterErrors errors) {
                bodyErrors.putAll(
                        StreamEx.of(errors.getFieldErrors())
                                .mapToEntry(
                                        FieldError::getField,
                                        fieldError -> messageSource.getMessage(fieldError, locale))
                                .collapseKeys()
                                .toMap());
            }

            @Override
            public void requestHeader(RequestHeader requestHeader, ParameterValidationResult result) {
                headerErrors.put(
                        Optional.of(requestHeader.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(result.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve request header name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestParam(@Nullable RequestParam requestParam, ParameterValidationResult result) {
                paramErrors.put(
                        Optional.ofNullable(requestParam)
                                .map(RequestParam::name)
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(result.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve request parameter name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestPart(RequestPart requestPart, ParameterErrors errors) {
                partErrors.put(
                        Optional.of(requestPart.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() -> Optional.of(errors.getMethodParameter())
                                                  .map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve request part name for %s"
                                                .formatted(errors.getMethodParameter()))),
                        StreamEx.of(errors.getFieldErrors())
                                .mapToEntry(
                                        FieldError::getField,
                                        fieldError -> messageSource.getMessage(fieldError, locale))
                                .collapseKeys()
                                .toMap());
            }

            @Override
            public void other(ParameterValidationResult result) {
                otherErrors.put(
                        Optional.of(result.getMethodParameter())
                                .map(MethodParameter::getParameterName)
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve name for %s".formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }
        });

        if (!cookieErrors.isEmpty()) {
            problemDetail.setProperty("cookieErrors", cookieErrors);
        }
        if (!modelErrors.isEmpty()) {
            problemDetail.setProperty("modelErrors", modelErrors);
        }
        if (!pathErrors.isEmpty()) {
            problemDetail.setProperty("pathErrors", pathErrors);
        }
        if (!bodyErrors.isEmpty()) {
            problemDetail.setProperty("bodyErrors", bodyErrors);
        }
        if (!headerErrors.isEmpty()) {
            problemDetail.setProperty("headerErrors", headerErrors);
        }
        if (!paramErrors.isEmpty()) {
            problemDetail.setProperty("paramErrors", paramErrors);
        }
        if (!partErrors.isEmpty()) {
            problemDetail.setProperty("partErrors", partErrors);
        }
        if (!otherErrors.isEmpty()) {
            problemDetail.setProperty("otherErrors", otherErrors);
        }

        return problemDetail;
    }

    @ExceptionHandler
    private ProblemDetail handleWebExchangeBindException(WebExchangeBindException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request.");

        val methodParameter = e.getMethodParameter();
        if (methodParameter != null) {
            val fieldErrors =
                    StreamEx.of(e.getFieldErrors())
                            .mapToEntry(
                                    FieldError::getField,
                                    fieldError -> messageSource.getMessage(fieldError, locale))
                            .collapseKeys()
                            .toMap();

            if (methodParameter.hasParameterAnnotation(RequestBody.class)) {
                problemDetail.setProperty("bodyErrors", fieldErrors);
            } else if (methodParameter.hasParameterAnnotation(ModelAttribute.class)) {
                problemDetail.setProperty("modelErrors", fieldErrors);
            } else if (methodParameter.hasParameterAnnotation(RequestPart.class)) {
                problemDetail.setProperty("partErrors", fieldErrors);
            } else {
                problemDetail.setProperty("otherErrors", fieldErrors);
            }
        }

        return problemDetail;
    }

    @ExceptionHandler
    private ProblemDetail handleErrorResponseException(ErrorResponseException e, Locale locale) {
        return e.updateAndGetBody(messageSource, locale);
    }

    @ExceptionHandler
    private ProblemDetail handleAxonException(AxonException e) {
        log.error("AxonFramework failure", e);

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler
    private ProblemDetail handleWebClientException(WebClientException e) {
        log.error("WebClient failure", e);

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler
    private ProblemDetail handleCallNotPermittedException(CallNotPermittedException e) {
        log.error(e.getMessage());

        return ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler
    private ProblemDetail handleTimeoutException(TimeoutException e) {
        log.trace("Operation timeout exceeded", e);

        return ProblemDetail.forStatusAndDetail(HttpStatus.GATEWAY_TIMEOUT, "Operation timeout exceeded.");
    }

    @ExceptionHandler
    private Mono<Void> handleAbortedException(AbortedException e, ServerWebExchange exchange) {
        log.trace("Inbound connection aborted", e);

        exchange.getResponse().setStatusCode(HttpStatus.REQUEST_TIMEOUT);
        return exchange.getResponse().setComplete();
    }

    @ExceptionHandler
    @SuppressWarnings("unused")
    private Object handleException(Exception e, ServerWebExchange exchange, Locale locale) {
        return switch (findCause(e, Predicates.<Throwable>falsePredicate()
                                              .or(ShowcaseCommandException.class::isInstance)
                                              .or(ShowcaseQueryException.class::isInstance)
                                              .or(AxonException.class::isInstance)
                                              .or(WebClientException.class::isInstance)
                                              .or(CallNotPermittedException.class::isInstance)
                                              .or(TimeoutException.class::isInstance)
                                              .or(AbortedException.class::isInstance))
                               .orElse(e)) {
            case ShowcaseCommandException ex -> handleShowcaseCommandException(ex);
            case ShowcaseQueryException ex -> handleShowcaseQueryException(ex);
            case AxonException ex -> handleAxonException(ex);
            case HandlerMethodValidationException ex -> handleHandlerMethodValidationException(ex, locale);
            case WebExchangeBindException ex -> handleWebExchangeBindException(ex, locale);
            case ErrorResponseException ex -> handleErrorResponseException(ex, locale);
            case WebClientException ex -> handleWebClientException(ex);
            case CallNotPermittedException ex -> handleCallNotPermittedException(ex);
            case TimeoutException ex -> handleTimeoutException(ex);
            case AbortedException ex -> handleAbortedException(ex, exchange);
            default -> {
                log.error("Unknown error", e);

                yield ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
            }
        };
    }

    private Optional<Throwable> findCause(Throwable t, Predicate<Throwable> predicate) {
        while (t != null) {
            if (predicate.test(t)) {
                return Optional.of(t);
            }
            t = t.getCause();
        }
        return Optional.empty();
    }
}
