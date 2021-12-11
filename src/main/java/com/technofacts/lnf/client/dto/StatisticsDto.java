package com.technofacts.lnf.client.dto;

import lombok.*;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsDto {

    Integer year;

    String month;

    Integer count;

}
