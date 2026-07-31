package com.fantasylol.backend.service;

import com.fantasylol.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class NicknameGenerator {

    private static final int MAX_ATTEMPTS = 20;

    private static final List<String> ADJECTIVES = List.of(
            "텔탄", "빡친", "숨은", "던지는", "빡겜", "즐겜", "캐리한", "우리팀", "풀피", "딸피",
            "무적", "로밍", "핑찍는", "솔용", "풀캠", "갱가는", "서렌친", "궁쓴", "못큰", "잘큰",
            "저격한", "막타친", "분노의", "취한", "썩은", "던지는", "노련한", "현란한", "안죽는", "못하는",
            "잘하는", "S급", "A급", "스틸한", "펜타킬", "한타의", "솔킬딴", "나홀로", "게으른", "불타는",
            "날뛰는", "빛나는", "책읽는", "답없는", "무적의", "지독한", "어쩌다", "갑자기", "협곡의", "속박된"
    );

    private static final List<String> NOUNS = List.of(
            "바론", "장로", "와드", "포탑", "서폿", "탑", "점멸", "강타", "블루", "레드",
            "핑와", "물약", "전령", "미드", "원딜", "정글", "유미", "야스오", "티모", "리신",
            "제드", "아리", "샤코", "가렌", "렉사이", "미니언", "바위게", "억제기", "넥서스", "챌린저",
            "마스터", "다이아", "골드", "실버", "브론즈", "아이언", "유저", "신발", "모자", "게이머",
            "중독자", "롤붕이", "검사", "마법사", "암살자", "힐러", "탱커", "선수", "프로", "찐팬"
    );

    private final UserRepository userRepository;

    public String generate() {

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = randomCandidate();

            if (!userRepository.existsByUsername(candidate)) {
                return candidate;
            }
        }

        log.warn("닉네임 후보가 {}번 연속 중복되어 대체 방식으로 생성", MAX_ATTEMPTS);

        return NOUNS.get(ThreadLocalRandom.current().nextInt(NOUNS.size())) + UUID.randomUUID().toString().substring(0, 8);

    }

    private String randomCandidate() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(random.nextInt(NOUNS.size()));
        String number = String.format("%04d", random.nextInt(10000));

        return adjective + noun + number;
    }

}
