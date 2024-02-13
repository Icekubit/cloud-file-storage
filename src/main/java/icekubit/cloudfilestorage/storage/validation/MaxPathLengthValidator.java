package icekubit.cloudfilestorage.storage.validation;

import icekubit.cloudfilestorage.storage.dto.Validatable;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class MaxPathLengthValidator implements
        ConstraintValidator<MaxPathLengthConstraint, Validatable> {

    @Override
    public void initialize(MaxPathLengthConstraint name) {
    }

    @Override
    public boolean isValid(Validatable form, ConstraintValidatorContext context) {
        return form.getCurrentPath().length() + form.getObjectName().length() <= 1000;

    }
}
