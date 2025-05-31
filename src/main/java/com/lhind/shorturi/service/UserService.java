package com.lhind.shorturi.service;

import com.lhind.shorturi.entity.Role;
import com.lhind.shorturi.entity.User;
import com.lhind.shorturi.entity.dto.request.RegisterRequestDto;
import com.lhind.shorturi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;

    public User save(RegisterRequestDto request) throws Exception {

        checkIfUserByUsernameExistAndIsActive(request);

        try {

            User user = User
                    .builder()
                    .username(request.username())
                    .password((request.password()))
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .isActive(true)
                    .build();

            return userRepository.save(user);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

    private void checkIfUserByUsernameExistAndIsActive(RegisterRequestDto request) throws Exception {
        Optional<User> optionalUser = userRepository.findByUsername(request.username());
        if (optionalUser.isPresent() && optionalUser.get().isActive()) {
            throw new Exception(String.format("User by username: %s already exists", request.username()));
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(String.format("User by %s not found", username)));

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username);
    }
}
