package com.toplms.master.users;

import com.toplms.core.exception.UserNotFoundException;
import com.toplms.domain.base.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByEmail(String email) {

            return this.userRepository.findByEmail(email);

    }

    public User create(User newUser, String rawPassword) {
        try {
            // 3. Hash the raw password before saving
            String hashedPassword = passwordEncoder.encode(rawPassword);
            newUser.setPassword(hashedPassword);

            return userRepository.save(newUser);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
