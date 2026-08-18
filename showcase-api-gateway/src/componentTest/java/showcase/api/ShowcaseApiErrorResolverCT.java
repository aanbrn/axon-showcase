package showcase.api;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.method.MethodValidationResult;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Showcase API error resolver component tests")
class ShowcaseApiErrorResolverCT {

    @SuppressWarnings("unused")
    static class Payload {
        private final String name = "value";
    }

    @SuppressWarnings("unused")
    static class Controller {
        public void handle(
                @CookieValue(name = "cookie") String cookie,
                @MatrixVariable(name = "matrix") String matrix,
                @ModelAttribute(name = "model") Payload model,
                @PathVariable(name = "id") String id,
                @RequestBody Payload body,
                @RequestHeader(name = "h") String header,
                @RequestParam(name = "p") String param,
                @RequestPart(name = "part") Payload part,
                String other) {
        }
    }

    private static final int COOKIE = 0;
    private static final int MATRIX = 1;
    private static final int MODEL = 2;
    private static final int PATH = 3;
    private static final int BODY = 4;
    private static final int HEADER = 5;
    private static final int PARAM = 6;
    private static final int PART = 7;
    private static final int OTHER = 8;

    private final Method method = controllerMethod();
    private final MessageSource messageSource = new ResourceBundleMessageSource();
    private final ShowcaseApiErrorResolver resolver = new ShowcaseApiErrorResolver(messageSource);

    @Test
    @DisplayName("Resolving a cookie value error produces cookie errors")
    void resolve_cookieValue_producesCookieErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(COOKIE), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("cookieErrors", Map.of("cookie", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a matrix variable error produces path errors")
    void resolve_matrixVariable_producesPathErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(MATRIX), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("pathErrors", Map.of("matrix", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a model attribute error produces model errors")
    void resolve_modelAttribute_producesModelErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(MODEL), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties())
                .containsEntry("modelErrors", Map.of("model", Map.of("name", List.of("bad name"))));
    }

    @Test
    @DisplayName("Resolving a path variable error produces path errors")
    void resolve_pathVariable_producesPathErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(PATH), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("pathErrors", Map.of("id", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a request body error produces body errors")
    void resolve_requestBody_producesBodyErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(BODY), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("bodyErrors", Map.of("name", List.of("bad name")));
    }

    @Test
    @DisplayName("Resolving a request header error produces header errors")
    void resolve_requestHeader_producesHeaderErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(HEADER), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("headerErrors", Map.of("h", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a request parameter error produces param errors")
    void resolve_requestParam_producesParamErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(PARAM), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("paramErrors", Map.of("p", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a request part error produces part errors")
    void resolve_requestPart_producesPartErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(PART), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties())
                .containsEntry("partErrors", Map.of("part", Map.of("name", List.of("bad name"))));
    }

    @Test
    @DisplayName("Resolving an other error produces other errors")
    void resolve_other_producesOtherErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionAt(OTHER), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("otherErrors", Map.of("other", List.of("boom")));
    }

    @Test
    @DisplayName("Resolving a web exchange bind exception for a request body produces body errors")
    void resolve_webExchangeBindException_requestBody_producesBodyErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(new WebExchangeBindException(parameter(BODY), bindingWithError()), Locale.ENGLISH,
                         problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("bodyErrors", Map.of("name", List.of("bad name")));
    }

    @Test
    @DisplayName("Resolving a web exchange bind exception for a model attribute produces model errors")
    void resolve_webExchangeBindException_modelAttribute_producesModelErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(new WebExchangeBindException(parameter(MODEL), bindingWithError()), Locale.ENGLISH,
                         problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("modelErrors", Map.of("name", List.of("bad name")));
    }

    @Test
    @DisplayName("Resolving a web exchange bind exception for a request part produces part errors")
    void resolve_webExchangeBindException_requestPart_producesPartErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(new WebExchangeBindException(parameter(PART), bindingWithError()), Locale.ENGLISH,
                         problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("partErrors", Map.of("name", List.of("bad name")));
    }

    @Test
    @DisplayName("Resolving a web exchange bind exception without a recognized annotation produces other errors")
    void resolve_webExchangeBindException_other_producesOtherErrors() {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(new WebExchangeBindException(parameter(OTHER), bindingWithError()), Locale.ENGLISH,
                         problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry("otherErrors", Map.of("name", List.of("bad name")));
    }

    private HandlerMethodValidationException exceptionAt(int index) {
        val result = switch (index) {
            case MODEL, BODY, PART -> (ParameterValidationResult) errors(index, bindingWithError());
            default -> validationResult(index);
        };
        return new HandlerMethodValidationException(
                MethodValidationResult.create(new Controller(), method, List.of(result)));
    }

    private ParameterValidationResult validationResult(int index) {
        return new ParameterValidationResult(
                parameter(index),
                new Object[0],
                List.of(new DefaultMessageSourceResolvable(new String[] { "error.code" }, "boom")),
                null,
                null,
                null,
                (resolvable, type) -> String.class);
    }

    private ParameterErrors errors(int index, BindingResult binding) {
        return new ParameterErrors(parameter(index), new Object[0], binding, null, null, null);
    }

    private BindingResult bindingWithError() {
        val binding = new BeanPropertyBindingResult(new Payload(), "payload");
        binding.addError(new FieldError("payload", "name", "bad name"));
        return binding;
    }

    private MethodParameter parameter(int index) {
        val methodParameter = new MethodParameter(method, index);
        methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        return methodParameter;
    }

    private static Method controllerMethod() {
        try {
            return Controller.class.getMethod(
                    "handle",
                    String.class, String.class, Payload.class, String.class, Payload.class,
                    String.class, String.class, Payload.class, String.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
}
