package icekubit.cloudfilestorage.storage.validation;

import icekubit.cloudfilestorage.storage.dto.Validatable;
import icekubit.cloudfilestorage.storage.service.MinioService;
import icekubit.cloudfilestorage.auth.model.CustomUserDetails;
import io.minio.messages.Item;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UniqueItemNameValidator implements
        ConstraintValidator<UniqueItemNameConstraint, Validatable> {

    @Autowired
    private MinioService minioService;

    @Override
    public void initialize(UniqueItemNameConstraint name) {
    }

    @Override
    public boolean isValid(Validatable formDto, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = ((CustomUserDetails) authentication.getPrincipal()).getUserId();

        String currentPath = formDto.getCurrentPath();
        String objectName = formDto.getObjectName();


        return minioService.getListOfItems(userId, currentPath).stream()
                .map(Item::objectName)
                .map(Paths::get)
                .map(Path::getFileName)
                .map(Path::toString)
                .noneMatch(itemName -> itemName.equals(objectName));
    }
}
