package icekubit.cloudfilestorage.storage.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = FileNameValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FileNameConstraint {
    String message() default "Filename cannot contain any of these characters: \\, /, :, *, ?, \", <, >, |";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
