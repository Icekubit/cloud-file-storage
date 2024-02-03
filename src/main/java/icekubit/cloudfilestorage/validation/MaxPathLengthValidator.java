package icekubit.cloudfilestorage.validation;

import icekubit.cloudfilestorage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.dto.RenameFormDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MaxPathLengthValidator implements
        ConstraintValidator<MaxPathLengthConstraint, Object> {

    @Override
    public void initialize(MaxPathLengthConstraint name) {
    }

    @Override
    public boolean isValid(Object form, ConstraintValidatorContext context) {

        // please forgive me for this shitcode(((

        if (form instanceof RenameFormDto) {
            RenameFormDto renameFormDto = (RenameFormDto) form;
            Path path = Paths.get(renameFormDto.getRelativePathToObject()).getParent();
            String currentPath = "";
            if (path != null) {
                currentPath = path.toString();
            }

            return currentPath.length() + renameFormDto.getObjectName().length() <= 1000;
        }

        if (form instanceof CreateFolderFormDto) {
            CreateFolderFormDto createFolderFormDto = (CreateFolderFormDto) form;
            return createFolderFormDto.getPath().length() + createFolderFormDto.getFolderName().length() <= 1000;
        }
        throw new RuntimeException("MaxPathLengthConstraint was applied to wrong class");
    }
}
