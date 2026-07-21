package com.fantasylol.backend.dto;

import lombok.*;

public class ProTeamDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long proTeamId;
        private String fullName;
        private String shortName;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String shortName;
        private String status;
    }

}
