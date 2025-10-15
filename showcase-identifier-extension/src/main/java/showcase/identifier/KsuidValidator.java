package showcase.identifier;

import com.github.ksuid.Ksuid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class KsuidValidator implements ConstraintValidator<KSUID, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
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
