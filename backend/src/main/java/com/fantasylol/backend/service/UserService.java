package com.fantasylol.backend.service;

import com.fantasylol.backend.dto.UserDto;
import com.fantasylol.backend.entity.User;
import com.fantasylol.backend.entity.WithdrawalFeedback;
import com.fantasylol.backend.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamRosterRepository teamRosterRepository;
    private final UserScoreRepository userScoreRepository;
    private final WithdrawalFeedbackRepository withdrawalFeedbackRepository;

    private static final List<String> BANNED = List.of("시발","씨발","병신","새끼","좆","존나","ㅅㅂ","ㅄ","fuck","shit");

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

    public UserDto.Response updateNickname(OAuth2User oAuth2User, UserDto.NicknameUpdateRequest request) {

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        String nickname = request.getUsername() == null ? "" : request.getUsername().trim();
        boolean hasProfanity = BANNED.stream().anyMatch(w -> nickname.toLowerCase().contains(w));

        if (nickname.isEmpty() || nickname.length() > 10 || hasProfanity) {
            throw new IllegalArgumentException("닉네임이 유효하지 않습니다.");
        }

        user.setUsername(nickname);
        User saved = userRepository.save(user);

        return UserDto.Response.builder()
                .userId(saved.getUserId())
                .username(saved.getUsername())
                .email(saved.getEmail())
                .profileImageUrl(saved.getProfileImageUrl())
                .build();

    }

    @Transactional
    public void withdraw(OAuth2User oAuth2User, UserDto.WithdrawRequest request) {

        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseThrow();

        withdrawalFeedbackRepository.save(WithdrawalFeedback.builder()
                .userId(user.getUserId())
                .reason(request.getReason())
                .note(request.getNote())
                .build());

        teamRepository.findByUserUserId(user.getUserId()).ifPresent(team -> {

            teamRosterRepository.deleteAll(teamRosterRepository.findByTeamTeamId(team.getTeamId()));
            teamRepository.delete(team);

        });

        userScoreRepository.deleteAll(userScoreRepository.findByUserUserId(user.getUserId()));

        userRepository.delete(user);

    }

}
