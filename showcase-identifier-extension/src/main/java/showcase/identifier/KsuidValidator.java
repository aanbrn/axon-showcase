// SPDX-License-Identifier: MIT
package showcase.identifier;

import com.github.ksuid.Ksuid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jspecify.annotations.Nullable;

/**
 * Validates that the annotated value is a well-formed KSUID.
 */
public final class KsuidValidator implements ConstraintValidator<KSUID, String> {
    /**
     * Checks whether the given value is a valid KSUID.
     *
     * <p>A {@code null} value is considered valid.
     *
     * @param value   the value to validate, may be {@code null}
     * @param context the validation context
     * @return {@code true} if the value is valid or {@code null}, {@code false} otherwise
     */
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
