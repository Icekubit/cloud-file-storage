package icekubit.cloudfilestorage.storage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxPathLengthValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxPathLengthConstraint {
    String message() default "The path is too long";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}