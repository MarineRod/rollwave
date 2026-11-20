package com.rollwave.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.rollwave.dao.AppUserDao;
import com.rollwave.service.AppUserService;

@Configuration
public class DataInitializer {

	@Bean
	CommandLineRunner initUsers(AppUserDao appUserDao, AppUserService appUserService) {
		return args -> {
			createIfAbsent(appUserDao, appUserService, "Alice", "Martin", "alice@example.com", "Alice123!");
			createIfAbsent(appUserDao, appUserService, "Bob", "Durand", "bob@example.com", "Bob12345!");
		};
	}

	private void createIfAbsent(AppUserDao appUserDao, AppUserService appUserService,
                                String firstName, String lastName,
                                String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();

        if (appUserDao.findByEmail(normalizedEmail).isEmpty()) {
            appUserService.createUser(firstName, lastName, normalizedEmail, password);
        }
    }
}
