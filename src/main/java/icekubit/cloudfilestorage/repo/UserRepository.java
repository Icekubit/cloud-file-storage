package icekubit.cloudfilestorage.repo;

import icekubit.cloudfilestorage.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
    User findByName(String name);
}
