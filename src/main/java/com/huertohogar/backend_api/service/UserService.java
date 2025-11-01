package com.huertohogar.backend_api.service;

import com.huertohogar.backend_api.model.User;
import com.huertohogar.backend_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !(authentication instanceof AnonymousAuthenticationToken)) {
            return userRepository.findByEmail(authentication.getName());
        }
        return Optional.empty();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User createUser(User user) {
        // Normalizar email a minúsculas antes de guardar
        user.setEmail(user.getEmail().toLowerCase().trim());
        if (userRepository.existsByEmail(user.getEmail())) {
            return null;
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();
            existingUser.setName(userDetails.getName());
            existingUser.setPhoneNumber(userDetails.getPhoneNumber());
            existingUser.setAddress(userDetails.getAddress());

            // Update email if provided and different
            if (userDetails.getEmail() != null && !userDetails.getEmail().isEmpty()) {
                String normalizedEmail = userDetails.getEmail().toLowerCase().trim();
                // Check if email is already taken by another user
                Optional<User> existingUserWithEmail = userRepository.findByEmail(normalizedEmail);
                if (existingUserWithEmail.isPresent() && !existingUserWithEmail.get().getId().equals(id)) {
                    // Email is taken by another user, return null to indicate failure
                    return null;
                }
                existingUser.setEmail(normalizedEmail);
            }

            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            return userRepository.save(existingUser);
        }
        return null;
    }

    public Map<String, Object> login(String email, String password) {
        // Normalizar email a minúsculas para búsqueda
        String normalizedEmail = email.toLowerCase().trim();
        Optional<User> user = userRepository.findByEmail(normalizedEmail);
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            String token = jwtService.generateToken(user.get().getId().toString());
            Map<String, Object> response = new HashMap<>();
            response.put("user", user.get());
            response.put("token", token);
            return response;
        }
        return null;
    }

    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
}