package de.oth.muskelmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MuskelManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(MuskelManagementApplication.class, args);
    }
}
