package icekubit.cloudfilestorage.validation;

import icekubit.cloudfilestorage.validation.UniqueEmailConstraint;
import icekubit.cloudfilestorage.repo.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UniqueEmailValidator implements
        ConstraintValidator<UniqueEmailConstraint, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void initialize(UniqueEmailConstraint email) {
    }

    @Override
    public boolean isValid(String emailField, ConstraintValidatorContext context) {
        return userRepository.findByEmail(emailField) == null;
    }
}
