package com.vaultsphere.config;

import com.vaultsphere.model.AppUser;
import com.vaultsphere.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
 @Bean CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder){return args->{
   if(!users.existsByUsername("admin")){AppUser a=new AppUser();a.setUsername("admin");a.setEmail("admin@vaultsphere.local");a.setPassword(encoder.encode("ChangeMe@123"));a.setRole("ADMIN");a.setEnabled(true);users.save(a);}
 };}
}
