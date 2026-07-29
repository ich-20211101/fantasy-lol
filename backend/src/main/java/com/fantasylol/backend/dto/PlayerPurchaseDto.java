package com.fantasylol.backend.dto;

import lombok.*;

import java.util.List;

public class PlayerPurchaseDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Row {
        private Long playerId;
        private String name;
        private String team;
        private String pos;
        private Double price;
        private Boolean priceInsufficientData;
        private Double score;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<Row> rows;
        private String sourceSeasonName;
    }


}
