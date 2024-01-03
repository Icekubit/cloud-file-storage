package icekubit.cloudfilestorage;

import org.springframework.boot.SpringApplication;

public class TestCloudFileStorageApplication {
    public static void main(String[] args) {
        SpringApplication.from(CloudFileStorageApplication::main)
                .with(TestBeans.class).run(args);
    }
}
