package icekubit.cloudfilestorage;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestBeans {

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> container() {
        return new PostgreSQLContainer<>("postgres:latest");
    }
}
