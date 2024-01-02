package icekubit.cloudfilestorage.repo;

import icekubit.cloudfilestorage.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String name);
    @Query(value = "SELECT CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE " +
            "WHERE CONSTRAINT_SCHEMA = 'my_db' AND TABLE_NAME = 'users' AND COLUMN_NAME = :columnName", nativeQuery = true)
    String findConstraintNameByColumnName(@Param("columnName") String columnName);
}
