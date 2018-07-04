package com.cassandra.dto.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BettingDto {
    private String gameIssueNumber;
    private String bettingNumber;
    private double price;
    private String token;
}
