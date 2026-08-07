package showcase.command;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import org.hibernate.validator.constraints.Length;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Validates that the annotated value is a valid showcase title (non-blank and not exceeding {@link #MAX_LENGTH}).
 */
@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Length(max = ShowcaseTitle.MAX_LENGTH)
@ReportAsSingleViolation
public @interface ShowcaseTitle {
    /**
     * The maximum allowed title length.
     */
    int MAX_LENGTH = 255;

    String message() default "{showcase.ShowcaseTitle.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
