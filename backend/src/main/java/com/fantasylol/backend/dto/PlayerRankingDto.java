package com.fantasylol.backend.dto;

import lombok.*;

import java.util.List;

public class PlayerRankingDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        private int rank;
        private Long playerId;
        private String name;
        private String team;
        private String pos;
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
        private String seasonLabel;
    }

}
