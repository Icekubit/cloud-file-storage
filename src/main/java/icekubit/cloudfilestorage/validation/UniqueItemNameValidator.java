package icekubit.cloudfilestorage.validation;

import icekubit.cloudfilestorage.dto.CreateFolderFormDto;
import icekubit.cloudfilestorage.dto.RenameFormDto;
import icekubit.cloudfilestorage.minio.MinioService;
import icekubit.cloudfilestorage.repo.UserRepository;
import io.minio.messages.Item;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.file.Path;
import java.nio.file.Paths;

public class UniqueItemNameValidator implements
        ConstraintValidator<UniqueItemNameConstraint, Object> {

    @Autowired
    private MinioService minioService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UniqueItemNameConstraint name) {
    }

    @Override
    public boolean isValid(Object formDto, ConstraintValidatorContext context) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = 0;
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            User userDetails = (User) authentication.getPrincipal();
            String userName = userDetails.getUsername();
            userId = userRepository.findByName(userName).get().getId();
        }

        String currentPath = "";
        String objectName = "";

        // please forgive me for this shitcode(((
        // if you know more elegant solution, please let me know

        if (formDto instanceof RenameFormDto) {
            RenameFormDto renameFormDto = (RenameFormDto) formDto;

            Path path = Paths.get(renameFormDto.getRelativePathToObject()).getParent();
            if (path != null) {
                currentPath = path.toString();
            }
            objectName = renameFormDto.getObjectName();

        } else if (formDto instanceof CreateFolderFormDto) {
            CreateFolderFormDto createFolderFormDto = (CreateFolderFormDto) formDto;
            currentPath = createFolderFormDto.getPath();
            objectName = createFolderFormDto.getFolderName();
        } else {
            throw new RuntimeException("UniqueItemNameConstraint was applied to wrong class");
        }

        String newItemName = objectName;

        return minioService.getListOfItems(userId, currentPath).stream()
                .map(Item::objectName)
                .map(Paths::get)
                .map(Path::getFileName)
                .map(Path::toString)
                .noneMatch(itemName -> itemName.equals(newItemName));
    }
}
