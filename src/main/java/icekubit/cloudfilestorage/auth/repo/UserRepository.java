package icekubit.cloudfilestorage.auth.repo;

import icekubit.cloudfilestorage.auth.model.entity.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String name);
}
