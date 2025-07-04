package showcase.api;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import one.util.streamex.StreamEx;
import org.axonframework.common.AxonException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.channel.AbortedException;
import showcase.command.FinishShowcaseCommand;
import showcase.command.RemoveShowcaseCommand;
import showcase.command.ScheduleShowcaseCommand;
import showcase.command.ShowcaseCommandException;
import showcase.command.ShowcaseCommandOperations;
import showcase.command.StartShowcaseCommand;
import showcase.query.FetchShowcaseByIdQuery;
import showcase.query.FetchShowcaseListQuery;
import showcase.query.PageRequest;
import showcase.query.Showcase;
import showcase.query.ShowcaseQueryException;
import showcase.query.ShowcaseQueryOperations;
import showcase.query.ShowcaseStatus;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

@RestController
@RequestMapping("/showcases")
@Slf4j
final class ShowcaseApiController implements ShowcaseApi {

    static final String FETCH_ALL_CACHE_NAME = FetchShowcaseListQuery.class.getSimpleName();

    static final String FETCH_BY_ID_CACHE_NAME = FetchShowcaseByIdQuery.class.getSimpleName();

    private final ShowcaseCommandOperations commandOperations;

    private final ShowcaseQueryOperations queryOperations;

    private final Cache fetchAllCache;

    private final Cache fetchByIdCache;

    private final MessageSource messageSource;

    ShowcaseApiController(
            @NonNull ShowcaseCommandOperations commandOperations,
            @NonNull ShowcaseQueryOperations queryOperations,
            @NonNull CacheManager cacheManager,
            @NonNull MessageSource messageSource) {
        this.commandOperations = commandOperations;
        this.queryOperations = queryOperations;
        this.fetchAllCache = cacheManager.getCache(FETCH_ALL_CACHE_NAME);
        this.fetchByIdCache = cacheManager.getCache(FETCH_BY_ID_CACHE_NAME);
        this.messageSource = messageSource;
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Void>> schedule(@RequestBody @Valid ScheduleShowcaseRequest request) {
        return commandOperations
                       .schedule(ScheduleShowcaseCommand
                                         .builder()
                                         .showcaseId(request.getShowcaseId())
                                         .title(request.getTitle())
                                         .startTime(request.getStartTime())
                                         .duration(request.getDuration())
                                         .build())
                       .map(ResponseEntity::ok)
                       .defaultIfEmpty(
                               ResponseEntity
                                       .created(fromUriString("/showcases/")
                                                        .path(request.getShowcaseId())
                                                        .build()
                                                        .toUri())
                                       .build());
    }

    @PutMapping("/{showcaseId}/start")
    public Mono<Void> start(@PathVariable String showcaseId) {
        return commandOperations.start(
                StartShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @PutMapping("/{showcaseId}/finish")
    public Mono<Void> finish(@PathVariable String showcaseId) {
        return commandOperations.finish(
                FinishShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @DeleteMapping("/{showcaseId}")
    public Mono<Void> remove(@PathVariable String showcaseId) {
        return commandOperations.remove(
                RemoveShowcaseCommand
                        .builder()
                        .showcaseId(showcaseId)
                        .build());
    }

    @GetMapping
    @SuppressWarnings("LoggingSimilarMessage")
    public Flux<Showcase> fetchAll(
            @RequestParam(required = false) String title,
            @RequestParam(name = "status", required = false) List<ShowcaseStatus> statuses,
            @PageableDefault(
                    size = PageRequest.DEFAULT_PAGE_SIZE,
                    sort = "startTime",
                    direction = Direction.DESC
            ) Pageable pageable) {
        val pageRequest = PageRequest.from(pageable);
        val query =
                FetchShowcaseListQuery
                        .builder()
                        .title(title)
                        .statuses(statuses)
                        .pageRequest(pageRequest)
                        .build();
        return queryOperations
                       .fetchAll(query)
                       .collectList()
                       .doOnNext(showcases -> fetchAllCache.put(query, showcases))
                       .flatMapMany(Flux::fromIterable)
                       .onErrorResume(Predicate.not(ShowcaseQueryException.class::isInstance), t -> {
                           @SuppressWarnings("unchecked")
                           val showcases = (List<Showcase>) fetchAllCache.get(query, List.class);
                           if (showcases != null) {
                               log.warn("Fallback on {}, cause: {}", query, t.getMessage());
                               return Flux.fromIterable(showcases);
                           } else {
                               return Flux.error(t);
                           }
                       });
    }

    @GetMapping("/{showcaseId}")
    @SuppressWarnings("LoggingSimilarMessage")
    public Mono<Showcase> fetchById(@PathVariable String showcaseId) {
        val query =
                FetchShowcaseByIdQuery
                        .builder()
                        .showcaseId(showcaseId)
                        .build();
        return queryOperations
                       .fetchById(query)
                       .doOnNext(showcase -> fetchByIdCache.put(query, showcase))
                       .onErrorResume(Predicate.not(ShowcaseQueryException.class::isInstance), t -> {
                           val showcase = fetchByIdCache.get(query, Showcase.class);
                           if (showcase != null) {
                               log.warn("Fallback on {}, cause: {}", query, t.getMessage());
                               return Mono.just(showcase);
                           } else {
                               return Mono.error(t);
                           }
                       });
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleShowcaseCommandException(ShowcaseCommandException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail = switch (errorDetails.getErrorCode()) {
            case INVALID_COMMAND -> {
                val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.getErrorMessage());
                pd.setProperty("fieldErrors", errorDetails.getMetaData());
                yield pd;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.getErrorMessage());
            case TITLE_IN_USE, ILLEGAL_STATE -> ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT, errorDetails.getErrorMessage());
        };
        problemDetail.setProperty("code", errorDetails.getErrorCode());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleShowcaseQueryException(ShowcaseQueryException e) {
        val errorDetails = e.getErrorDetails();
        val problemDetail = switch (errorDetails.getErrorCode()) {
            case INVALID_QUERY -> {
                val pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorDetails.getErrorMessage());
                pd.setProperty("fieldErrors", errorDetails.getMetaData());
                yield pd;
            }
            case NOT_FOUND -> ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, errorDetails.getErrorMessage());
        };
        problemDetail.setProperty("code", errorDetails.getErrorCode());
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleHandlerMethodValidationException(
            HandlerMethodValidationException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request parameter.");
        val parameterValidationResults = e.getParameterValidationResults();
        if (!parameterValidationResults.isEmpty()) {
            val parameterErrors =
                    StreamEx.of(parameterValidationResults)
                            .mapToEntry(
                                    validationResult -> validationResult.getMethodParameter().getParameterName(),
                                    validationResult -> StreamEx.of(validationResult.getResolvableErrors())
                                                                .map(error -> messageSource.getMessage(error, locale))
                                                                .toList())
                            .toMap();
            problemDetail.setProperty("parameterErrors", parameterErrors);
        }
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleWebExchangeBindException(WebExchangeBindException e, Locale locale) {
        val problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid request content.");
        if (e.getFieldErrorCount() > 0) {
            val resolvedLocale = Optional.ofNullable(locale).orElse(Locale.ENGLISH);
            val fieldErrors =
                    StreamEx.of(e.getFieldErrors())
                            .mapToEntry(
                                    FieldError::getField,
                                    fieldError -> messageSource.getMessage(fieldError, resolvedLocale))
                            .collapseKeys()
                            .toMap();
            problemDetail.setProperty("fieldErrors", fieldErrors);
        }
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleErrorResponseException(ErrorResponseException e, Locale locale) {
        val resolvedLocale = Optional.ofNullable(locale).orElse(Locale.ENGLISH);
        val problemDetail = e.updateAndGetBody(messageSource, resolvedLocale);
        return ResponseEntity.of(problemDetail).build();
    }

    @ExceptionHandler
    private ResponseEntity<ProblemDetail> handleException(Exception e) {
        switch (e) {
            case AxonException ex -> log.error("AxonFramework failure", ex);
            case WebClientException ex -> log.error("WebClient failure", ex);
            case BulkheadFullException ex -> log.error(ex.getMessage());
            case TimeoutException ex -> log.error(ex.getMessage());
            case CallNotPermittedException ex -> log.error(ex.getMessage());
            case AbortedException ex -> log.debug("Inbound connection aborted", ex);
            default -> log.error("Unknown error", e);
        }
        return ResponseEntity.of(ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE))
                             .build();
    }
}
