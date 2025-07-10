package com.pomodoro.app.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pomodoro.app.model.User;
import com.pomodoro.app.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public User registerNewUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Set creation and update timestamps
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Save user
        return userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUserSettings(User user, User updatedSettings) {
        // Update only the settings fields
        user.setFocusDuration(updatedSettings.getFocusDuration());
        user.setBreakDuration(updatedSettings.getBreakDuration());
        user.setLongBreakDuration(updatedSettings.getLongBreakDuration());
        user.setPomodorosUntilLongBreak(updatedSettings.getPomodorosUntilLongBreak());
        user.setAutoStartBreaks(updatedSettings.getAutoStartBreaks());
        user.setAutoStartPomodoros(updatedSettings.getAutoStartPomodoros());
        user.setDailyTarget(updatedSettings.getDailyTarget());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
}