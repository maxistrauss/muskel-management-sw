package de.oth.muskelmanagement.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserConverter userConverter;
    private final RoomConverter roomConverter;

    public WebConfig(UserConverter userConverter, RoomConverter roomConverter) {
        this.userConverter = userConverter;
        this.roomConverter = roomConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(userConverter);
        registry.addConverter(roomConverter);
    }
}
