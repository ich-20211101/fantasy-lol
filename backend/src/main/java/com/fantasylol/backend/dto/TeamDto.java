package com.fantasylol.backend.dto;

import lombok.*;

import java.util.List;

public class TeamDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String teamName;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RosterSubmitRequest{
        private List<Long> playerIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StarterUpdateRequest{
        private List<Long> playerIds;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long teamId;
        private String teamName;
        private boolean rosterLocked;
        private List<RosterPlayerResponse> roster;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RosterPlayerResponse {
        private Long teamRosterId;
        private Long playerId;
        private String playerName;
        private String position;
        private String teamName;
        private boolean isStarter;
    }

}
