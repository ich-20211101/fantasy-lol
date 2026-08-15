package com.fantasylol.backend.dto;

import lombok.*;

public class SeasonDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long seasonId;
        private String seasonName;
        private String status;
        private Boolean featured;
        private Integer minGamesForRanking;
        private Boolean rosterSourceSeason;
    }

}
