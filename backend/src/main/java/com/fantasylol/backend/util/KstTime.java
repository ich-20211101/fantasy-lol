package com.fantasylol.backend.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class KstTime {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private KstTime() {}

    public static LocalDate toKstDate(LocalDateTime utcDateTime) {
        return utcDateTime.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(KST)
                .toLocalDate();
    }

    public static LocalDateTime nowUtc() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    public static LocalDate nowKstDate() {
        return LocalDate.now(KST);
    }

}
