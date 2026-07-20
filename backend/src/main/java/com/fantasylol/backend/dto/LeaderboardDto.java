package com.fantasylol.backend.dto;

import lombok.*;

import java.util.List;

public class LeaderboardDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        private int rank;
        private String team;
        private String owner;
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<Row> rows;
        private boolean hasMore;
        private boolean tallying;
        private Integer weekNumber;
        private String seasonName;
        private String seasonLabel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Round {
        private String seasonName;
        private String seasonLabel;
        private List<Integer> weeks;
    }

}
