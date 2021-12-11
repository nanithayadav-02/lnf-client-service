package com.technofacts.lnf.client.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDashboardDto {

    private long totalClients = 0;

    private List<StatisticsDto> statistics = new ArrayList<>();

}
