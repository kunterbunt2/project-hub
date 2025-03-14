package de.bushnaq.abdalla.projecthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("de.bushnaq.abdalla.projecthub")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
/*
    @Bean
    CommandLineRunner initialize(UserRepository userRepository) {
        return args -> {
            Stream.of("John", "Robert", "Nataly", "Helen", "Mary").forEach(name -> {
                User user = new User(name);
                userRepository.save(user);
            });
            userRepository.findAll().forEach(System.out::println);
        };
    }

 */
}