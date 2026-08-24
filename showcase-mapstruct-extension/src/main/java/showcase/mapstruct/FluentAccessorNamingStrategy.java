// SPDX-License-Identifier: MIT
package showcase.mapstruct;

import javax.lang.model.element.ExecutableElement;
import lombok.val;
import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;

/**
 * MapStruct naming strategy supporting fluent accessors, where a getter is a parameterless method matching a field
 * by name and return type.
 */
public final class FluentAccessorNamingStrategy extends DefaultAccessorNamingStrategy {
    /**
     * Returns the property name for the given getter or setter method.
     *
     * <p>For fluent getters, the property name is the method name itself; otherwise the default strategy is used.
     *
     * @param getterOrSetterMethod the getter or setter method
     * @return the property name
     */
    @Override
    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        if (isFluentGetter(getterOrSetterMethod)) {
            return getterOrSetterMethod.getSimpleName().toString();
        } else {
            return super.getPropertyName(getterOrSetterMethod);
        }
    }

    /**
     * Checks whether the given method is a getter, either a standard getter or a fluent one.
     *
     * @param method the method to inspect
     * @return {@code true} if the method is a getter
     */
    @Override
    public boolean isGetterMethod(ExecutableElement method) {
        return isFluentGetter(method) || super.isGetterMethod(method);
    }

    /**
     * Checks whether the given parameterless method is a fluent getter for a field of the same name and type.
     *
     * @param method the method to inspect
     * @return {@code true} if the method is a fluent getter
     */
    @SuppressWarnings("ConstantValue")
    private boolean isFluentGetter(ExecutableElement method) {
        if (method.getParameters().isEmpty() && method.getEnclosingElement() != null) {
            for (val element : method.getEnclosingElement().getEnclosedElements()) {
                if (element.getKind().isField()
                        && element.getSimpleName().equals(method.getSimpleName())
                        && typeUtils.isSameType(element.asType(), method.getReturnType())) {
                    return true;
                }
            }
        }
        return false;
    }
}
