package showcase.command;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import org.hibernate.validator.constraints.time.DurationMax;
import org.hibernate.validator.constraints.time.DurationMin;

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

@Documented
@Constraint(validatedBy = {})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@DurationMin(minutes = ShowcaseDuration.MIN_MINUTES)
@DurationMax(minutes = ShowcaseDuration.MAX_MINUTES)
@ReportAsSingleViolation
public @interface ShowcaseDuration {

    int MIN_MINUTES = 1;

    int MAX_MINUTES = 10;

    String message() default "{showcase.ShowcaseDuration.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
