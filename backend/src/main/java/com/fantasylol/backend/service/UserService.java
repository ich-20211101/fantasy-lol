package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.UserDto;
import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDto.Response createUser(UserDto.Request request) {
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .build();

        User saved = userRepository.save(user);

        return UserDto.Response.builder()
                .userId(saved.getUserId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .build();
    }

    public UserDto.Response getCurrentUser(OAuth2User oauth2User) {

        String email = oauth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        return UserDto.Response.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .build();

    }

}
