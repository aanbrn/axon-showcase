package showcase;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ulid4j.Ulid;

public final class UlidValidator implements ConstraintValidator<ULID, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return Ulid.isValid(value);
    }
}
