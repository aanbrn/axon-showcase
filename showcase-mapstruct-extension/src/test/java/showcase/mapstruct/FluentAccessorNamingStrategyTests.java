package showcase.mapstruct;

import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.ap.spi.MapStructProcessingEnvironment;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FluentAccessorNamingStrategyTests {

    private FluentAccessorNamingStrategy strategy;
    private final Map<String, Name> names = new HashMap<>();
    private final Map<String, TypeMirror> types = new HashMap<>();

    @BeforeEach
    void setUp() {
        strategy = new FluentAccessorNamingStrategy();
        val typeUtils = mock(Types.class);
        when(typeUtils.isSameType(any(), any()))
                .thenAnswer(invocation -> Objects.equals(invocation.getArgument(0), invocation.getArgument(1)));
        val env = mock(MapStructProcessingEnvironment.class);
        when(env.getTypeUtils()).thenReturn(typeUtils);
        when(env.getElementUtils()).thenReturn(mock(Elements.class));
        when(env.getOptions()).thenReturn(Map.of());
        strategy.init(env);
    }

    @Test
    void isGetterMethod_parameterlessMethodMatchingField_returnsTrue() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "showcaseId", "java.lang.String");

        assertThat(strategy.isGetterMethod(method)).isTrue();
    }

    @Test
    void isGetterMethod_parameterlessMethodWithoutMatchingField_returnsFalse() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "computedValue", "java.lang.String");

        assertThat(strategy.isGetterMethod(method)).isFalse();
    }

    @Test
    void isGetterMethod_methodWithParameters_returnsFalse() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "showcaseId", "java.lang.String", true);

        assertThat(strategy.isGetterMethod(method)).isFalse();
    }

    @Test
    void isGetterMethod_methodWithDifferentReturnType_returnsFalse() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "showcaseId", "int");

        assertThat(strategy.isGetterMethod(method)).isFalse();
    }

    @Test
    void getPropertyName_fluentGetter_returnsMethodSimpleName() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "showcaseId", "java.lang.String");

        assertThat(strategy.getPropertyName(method)).isEqualTo("showcaseId");
    }

    @Test
    void getPropertyName_standardGetter_usesDefaultStrategy() {
        val clazz = buildClass(buildField("showcaseId", "java.lang.String"));
        val method = buildMethod(clazz, "getShowcaseId", "java.lang.String");

        assertThat(strategy.getPropertyName(method)).isEqualTo("showcaseId");
    }

    @Test
    void isGetterMethod_coexistenceOfFluentAndStandardGetters_isRecognized() {
        val clazz = buildClass(buildField("title", "java.lang.String"),
                               buildField("description", "java.lang.String"));
        val fluentGetter = buildMethod(clazz, "title", "java.lang.String");
        val standardGetter = buildMethod(clazz, "getDescription", "java.lang.String");

        assertThat(strategy.isGetterMethod(fluentGetter)).isTrue();
        assertThat(strategy.isGetterMethod(standardGetter)).isTrue();
        assertThat(strategy.getPropertyName(standardGetter)).isEqualTo("description");
    }

    @Test
    void spiRegistration_listsFluentAccessorNamingStrategy() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(
                "META-INF/services/org.mapstruct.ap.spi.AccessorNamingStrategy")) {
            assertThat(in).isNotNull();
            val content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertThat(content).contains("showcase.mapstruct.FluentAccessorNamingStrategy");
        }
    }

    private Element buildField(String name, String type) {
        val nameMock = name(name);
        val typeMock = type(type);
        val field = mock(Element.class);
        when(field.getKind()).thenReturn(ElementKind.FIELD);
        when(field.getSimpleName()).thenReturn(nameMock);
        when(field.asType()).thenReturn(typeMock);
        return field;
    }

    private TypeElement buildClass(Element... fields) {
        val clazz = mock(TypeElement.class);
        doReturn(java.util.Arrays.asList(fields)).when(clazz).getEnclosedElements();
        return clazz;
    }

    private ExecutableElement buildMethod(TypeElement clazz, String name, String returnType) {
        return buildMethod(clazz, name, returnType, false);
    }

    private ExecutableElement buildMethod(TypeElement clazz, String name, String returnType, boolean withParameter) {
        val nameMock = name(name);
        val returnTypeMock = type(returnType);
        val method = mock(ExecutableElement.class);
        when(method.getEnclosingElement()).thenReturn(clazz);
        when(method.getSimpleName()).thenReturn(nameMock);
        when(method.getReturnType()).thenReturn(returnTypeMock);
        if (withParameter) {
            doReturn(java.util.Collections.singletonList(mock(VariableElement.class)))
                    .when(method).getParameters();
        }
        return method;
    }

    private Name name(String value) {
        return names.computeIfAbsent(value, key -> {
            val nameMock = mock(Name.class);
            when(nameMock.toString()).thenReturn(key);
            return nameMock;
        });
    }

    private TypeMirror type(String value) {
        return types.computeIfAbsent(value, key -> mock(TypeMirror.class));
    }
}
