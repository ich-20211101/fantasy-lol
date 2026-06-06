package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.UserDto;
import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

}
