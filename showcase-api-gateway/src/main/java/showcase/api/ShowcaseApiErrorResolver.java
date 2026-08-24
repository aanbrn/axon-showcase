// SPDX-License-Identifier: MIT
package showcase.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.val;
import one.util.streamex.StreamEx;
import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException.Visitor;

/**
 * Resolves Spring framework validation exceptions into per-parameter error maps on a {@link ProblemDetail}.
 *
 * <p>Fills the problem detail's error properties (cookie, model, path, body, header, param, part, and other errors)
 * from the validation results carried by the exception, resolving each message through the {@link MessageSource}.
 */
@Component
@RequiredArgsConstructor
class ShowcaseApiErrorResolver {
    /**
     * Resolves error messages for the validation errors.
     */
    private final MessageSource messageSource;

    /**
     * Resolves a {@link HandlerMethodValidationException} into per-parameter error maps on the given problem detail.
     *
     * <p>Visits each validation result and groups its errors by parameter kind, setting the corresponding problem
     * detail property only when errors are present.
     *
     * @param e             the handler method validation exception to resolve
     * @param locale        the locale used to resolve error messages
     * @param problemDetail the problem detail to populate with the error maps
     */
    void resolve(HandlerMethodValidationException e, Locale locale, ProblemDetail problemDetail) {
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
                                .or(() ->
                                        Optional.of(result.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(() -> new IllegalStateException(
                                        "Unable to resolve cookie name for %s".formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .filter(Objects::nonNull)
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void matrixVariable(MatrixVariable matrixVariable, ParameterValidationResult result) {
                pathErrors.put(
                        Optional.of(matrixVariable.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() ->
                                        Optional.of(result.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(
                                        () -> new IllegalStateException("Unable to resolve matrix variable name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .filter(Objects::nonNull)
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void modelAttribute(@Nullable ModelAttribute modelAttribute, ParameterErrors errors) {
                modelErrors.put(
                        Optional.ofNullable(modelAttribute)
                                .map(ModelAttribute::name)
                                .filter(Predicate.not(String::isBlank))
                                .or(() ->
                                        Optional.of(errors.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(
                                        () -> new IllegalStateException("Unable to resolve model attribute name for %s"
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
                                .or(() ->
                                        Optional.of(result.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(
                                        () -> new IllegalStateException("Unable to resolve path variable name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .filter(Objects::nonNull)
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestBody(RequestBody requestBody, ParameterErrors errors) {
                bodyErrors.putAll(StreamEx.of(errors.getFieldErrors())
                        .mapToEntry(FieldError::getField, fieldError -> messageSource.getMessage(fieldError, locale))
                        .collapseKeys()
                        .toMap());
            }

            @Override
            public void requestHeader(RequestHeader requestHeader, ParameterValidationResult result) {
                headerErrors.put(
                        Optional.of(requestHeader.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() ->
                                        Optional.of(result.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(
                                        () -> new IllegalStateException("Unable to resolve request header name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .filter(Objects::nonNull)
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestParam(@Nullable RequestParam requestParam, ParameterValidationResult result) {
                paramErrors.put(
                        Optional.ofNullable(requestParam)
                                .map(RequestParam::name)
                                .filter(Predicate.not(String::isBlank))
                                .or(() ->
                                        Optional.of(result.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(() ->
                                        new IllegalStateException("Unable to resolve request parameter name for %s"
                                                .formatted(result.getMethodParameter()))),
                        StreamEx.of(result.getResolvableErrors())
                                .filter(Objects::nonNull)
                                .map(error -> messageSource.getMessage(error, locale))
                                .toList());
            }

            @Override
            public void requestPart(RequestPart requestPart, ParameterErrors errors) {
                partErrors.put(
                        Optional.of(requestPart.name())
                                .filter(Predicate.not(String::isBlank))
                                .or(() ->
                                        Optional.of(errors.getMethodParameter()).map(MethodParameter::getParameterName))
                                .orElseThrow(
                                        () -> new IllegalStateException("Unable to resolve request part name for %s"
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
                                .filter(Objects::nonNull)
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
    }
}
