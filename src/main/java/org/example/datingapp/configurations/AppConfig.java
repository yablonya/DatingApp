package org.example.datingapp.configurations;

import org.example.datingapp.repositories.ProfileRepository;
import org.example.datingapp.repositories.RelationsRepository;
import org.example.datingapp.services.ProfileService;
import org.example.datingapp.services.RelationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;


@Configuration
public class AppConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Dating App API Documentation")
                        .version("1.0.0")
                        .description("API documentation for the Dating App application"));
    }

    @Bean
    public ProfileService profileService(ProfileRepository profileRepository) {
        return new ProfileService(profileRepository, prototypeLogger());
    }

    @Bean
    public RelationService relationService(
            RelationsRepository relationsRepository,
            ProfileRepository profileRepository
    ) {
        return new RelationService(relationsRepository, profileRepository, prototypeLogger());
    }

    @Bean
    @Scope("prototype")
    public Logger prototypeLogger() {
        return LoggerFactory.getLogger("PrototypeLogger");
    }
}
