package icekubit.cloudfilestorage.validation;

import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueNameValidator implements
        ConstraintValidator<UniqueNameConstraint, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UniqueNameConstraint name) {
    }

    @Override
    public boolean isValid(String nameField, ConstraintValidatorContext context) {
        return userRepository.findByName(nameField) == null;
    }
}
