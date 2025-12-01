package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.User;
import de.oth.muskelmanagement.repository.UserRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserConverter implements Converter<String, User> {

    private final UserRepository userRepository;

    public UserConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User convert(String source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            Long userId = Long.parseLong(source);
            return userRepository.findById(userId).orElse(null);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
