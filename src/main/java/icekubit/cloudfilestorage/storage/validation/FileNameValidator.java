package icekubit.cloudfilestorage.storage.validation;

import icekubit.cloudfilestorage.storage.dto.UploadFileFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class FileNameValidator implements
        ConstraintValidator<FileNameConstraint, UploadFileFormDto> {

    @Override
    public void initialize(FileNameConstraint name) {
    }

    @Override
    public boolean isValid(UploadFileFormDto form, ConstraintValidatorContext context) {
        return form.getObjectName().matches("^(?![\\s\\S]*[/\\\\:*?\"<>|]).*");
    }
}