package icekubit.cloudfilestorage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueNameValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueNameConstraint {
    String message() default "The user with this name already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
