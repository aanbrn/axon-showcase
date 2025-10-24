package showcase.identifier;

import com.github.ksuid.Ksuid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

public final class KsuidValidator implements ConstraintValidator<KSUID, String> {

    @Override
    public boolean isValid(@Nullable String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        try {
            Ksuid.fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
