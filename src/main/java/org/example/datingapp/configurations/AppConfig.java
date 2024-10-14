package org.example.datingapp.configurations;

import org.example.datingapp.repositories.ProfileRepository;
import org.example.datingapp.services.ProfileService;
import org.example.datingapp.services.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;


@Configuration
public class AppConfig {
    @Bean
    public ProfileService profileService(ProfileRepository profileRepository) {
        return new ProfileService(profileRepository, prototypeLogger());
    }

    @Bean
    public RelationService relationService() {
        return new RelationService();
    }

    @Bean
    @Scope("prototype")
    public Logger prototypeLogger() {
        return LoggerFactory.getLogger("PrototypeLogger");
    }
}
