package com.fantasylol.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

public class TeamDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RosterSubmitRequest{
        private String teamName;
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

        @JsonProperty("isStarter")
        private boolean isStarter;
    }

}
