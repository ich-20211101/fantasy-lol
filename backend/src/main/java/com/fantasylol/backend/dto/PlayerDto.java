package com.fantasylol.backend.dto;

import lombok.*;

public class PlayerDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long playerId;
        private String playerName;
        private String position;
        private String teamName;
        private String status;
    }

}
