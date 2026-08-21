package showcase.api;

import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

@DisplayName("Showcase API error resolver component tests")
class ShowcaseApiErrorResolverCT {

    @SuppressWarnings("unused")
    static class Payload {
        private final String name = "value";
    }

    @SuppressWarnings("unused")
    static class Controller {
        void cookie(@CookieValue(name = "cookie") String cookie) {
        }

        void matrix(@MatrixVariable(name = "matrix") String matrix) {
        }

        void model(@ModelAttribute(name = "model") Payload model) {
        }

        void path(@PathVariable(name = "id") String id) {
        }

        void body(@RequestBody Payload body) {
        }

        void header(@RequestHeader(name = "h") String header) {
        }

        void param(@RequestParam(name = "p") String param) {
        }

        void part(@RequestPart(name = "part") Payload part) {
        }

        void other(String other) {
        }

        void paramNoName(@RequestParam String value) {
        }

        void pathNoName(@PathVariable String value) {
        }

        void otherMultiErrors(String other) {
        }
    }

    private static final String MULTI_ERROR_METHOD = "otherMultiErrors";

    private final MessageSource messageSource = new ResourceBundleMessageSource();
    private final ShowcaseApiErrorResolver resolver = new ShowcaseApiErrorResolver(messageSource);

    @ParameterizedTest
    @DisplayName("Resolving a parameter validation error produces the expected error map")
    @MethodSource
    void resolve_producesExpectedErrors(String methodName, String property, Object expected) {
        val problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        resolver.resolve(exceptionFor(methodName), Locale.ENGLISH, problemDetail);

        assertThat(problemDetail.getProperties()).containsEntry(property, expected);
    }

    static List<Arguments> resolve_producesExpectedErrors() {
        return List.of(
                argumentSet("cookie value", "cookie", "cookieErrors", Map.of("cookie", List.of("boom"))),
                argumentSet("matrix variable", "matrix", "pathErrors", Map.of("matrix", List.of("boom"))),
                argumentSet("model attribute", "model", "modelErrors",
                            Map.of("model", Map.of("name", List.of("bad name")))),
                argumentSet("path variable", "path", "pathErrors", Map.of("id", List.of("boom"))),
                argumentSet("request body", "body", "bodyErrors", Map.of("name", List.of("bad name"))),
                argumentSet("request header", "header", "headerErrors", Map.of("h", List.of("boom"))),
                argumentSet("request param", "param", "paramErrors", Map.of("p", List.of("boom"))),
                argumentSet("request part", "part", "partErrors",
                            Map.of("part", Map.of("name", List.of("bad name")))),
                argumentSet("unannotated parameter", "other", "otherErrors", Map.of("other", List.of("boom"))),
                argumentSet("request param without a name falls back to the parameter name",
                            "paramNoName", "paramErrors", Map.of("value", List.of("boom"))),
                argumentSet("path variable without a name falls back to the parameter name",
                            "pathNoName", "pathErrors", Map.of("value", List.of("boom"))),
                argumentSet("multiple errors on one parameter produce all messages",
                            "otherMultiErrors", "otherErrors", Map.of("other", List.of("boom 1", "boom 2"))));
    }

    private HandlerMethodValidationException exceptionFor(String methodName) {
        val method = controllerMethod(methodName);
        val parameter = parameter(method);
        val messages = MULTI_ERROR_METHOD.equals(methodName)
                               ? List.of(resolvable("boom 1"), resolvable("boom 2"))
                               : List.of(resolvable("boom"));
        val result = method.getParameterTypes()[0] == Payload.class
                             ? errors(parameter, bindingWithError())
                             : validationResult(parameter, messages);
        return new HandlerMethodValidationException(
                MethodValidationResult.create(new Controller(), method, List.of(result)));
    }

    private ParameterValidationResult validationResult(MethodParameter parameter,
                                                       List<MessageSourceResolvable> errors) {
        return new ParameterValidationResult(
                parameter,
                new Object[0],
                errors,
                null,
                null,
                null,
                (resolvable, type) -> String.class);
    }

    private static MessageSourceResolvable resolvable(String message) {
        return new DefaultMessageSourceResolvable(new String[] { "error.code" }, message);
    }

    private ParameterErrors errors(MethodParameter parameter, BindingResult binding) {
        return new ParameterErrors(parameter, new Object[0], binding, null, null, null);
    }

    private BindingResult bindingWithError() {
        val binding = new BeanPropertyBindingResult(new Payload(), "payload");
        binding.addError(new FieldError("payload", "name", "bad name"));
        return binding;
    }

    private MethodParameter parameter(Method method) {
        val methodParameter = new MethodParameter(method, 0);
        methodParameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        return methodParameter;
    }

    private static Method controllerMethod(String name) {
        return Arrays.stream(Controller.class.getDeclaredMethods())
                     .filter(method -> method.getName().equals(name))
                     .findFirst()
                     .orElseThrow(() -> new IllegalStateException("No controller method: " + name));
    }
}
