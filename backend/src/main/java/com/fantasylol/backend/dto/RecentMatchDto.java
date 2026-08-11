package com.fantasylol.backend.dto;

import lombok.*;

@Getter
@Builder
public class RecentMatchDto {

    private String dateTimeUtc;
    private String team1;
    private String team2;
    private int team1Score;
    private int team2Score;

}
