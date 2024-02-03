package icekubit.cloudfilestorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueItemNameValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueItemNameConstraint {
    String message() default "The object with this name already exists in the current folder";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
