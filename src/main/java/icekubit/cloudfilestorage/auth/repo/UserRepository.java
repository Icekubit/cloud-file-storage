package icekubit.cloudfilestorage.auth.repo;

import icekubit.cloudfilestorage.auth.model.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByName(String name);
    Optional<User> findByEmail(String name);
    @Query(value = "SELECT CONSTRAINT_NAME FROM information_schema.constraint_column_usage " +
            "WHERE TABLE_NAME = 'users' AND COLUMN_NAME = :columnName", nativeQuery = true)
    String findConstraintNameByColumnName(@Param("columnName") String columnName);
}
