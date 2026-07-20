package com.fantasylol.backend.dto;

import lombok.*;

public class UserScoreDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Integer rank;
        private Double score;
    }

}
