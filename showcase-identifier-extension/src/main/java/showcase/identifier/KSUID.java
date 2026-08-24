// SPDX-License-Identifier: MIT
package showcase.identifier;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import showcase.identifier.KSUID.List;

/**
 * Validates that the annotated value is a well-formed KSUID.
 */
@Documented
@Constraint(validatedBy = KsuidValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Repeatable(List.class)
public @interface KSUID {
    /**
     * The default validation message key.
     *
     * @return the message key
     */
    String message() default "{showcase.identifier.KSUID.message}";

    /**
     * The validation groups the constraint belongs to.
     *
     * @return the validation groups
     */
    Class<?>[] groups() default {};

    /**
     * The payload associated with the constraint.
     *
     * @return the payload type
     */
    Class<? extends Payload>[] payload() default {};

    /**
     * Holds a list of {@link KSUID} constraints for repeated usage.
     */
    @Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
    @Retention(RUNTIME)
    @Documented
    @interface List {
        KSUID[] value();
    }
}
