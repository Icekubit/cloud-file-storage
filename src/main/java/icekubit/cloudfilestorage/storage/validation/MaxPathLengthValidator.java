package icekubit.cloudfilestorage.storage.validation;

import icekubit.cloudfilestorage.storage.dto.FileOperationsDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class MaxPathLengthValidator implements
        ConstraintValidator<MaxPathLengthConstraint, FileOperationsDto> {

    @Override
    public void initialize(MaxPathLengthConstraint name) {
    }

    @Override
    public boolean isValid(FileOperationsDto form, ConstraintValidatorContext context) {
        return form.getCurrentPath().length() + form.getObjectName().length() <= 1000;

    }
}
