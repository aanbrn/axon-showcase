package showcase.mapstruct;

import lombok.val;
import org.mapstruct.ap.spi.DefaultAccessorNamingStrategy;

import javax.lang.model.element.ExecutableElement;

public final class FluentAccessorNamingStrategy extends DefaultAccessorNamingStrategy {

    @Override
    public String getPropertyName(ExecutableElement getterOrSetterMethod) {
        if (isFluentGetter(getterOrSetterMethod)) {
            return getterOrSetterMethod.getSimpleName().toString();
        } else {
            return super.getPropertyName(getterOrSetterMethod);
        }
    }

    @Override
    public boolean isGetterMethod(ExecutableElement method) {
        return isFluentGetter(method) || super.isGetterMethod(method);
    }

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
