package icekubit.cloudfilestorage.repo;

import icekubit.cloudfilestorage.model.entity.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String name);
    User findByEmail(String name);
}
