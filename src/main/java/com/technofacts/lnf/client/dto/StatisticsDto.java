package com.technofacts.lnf.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(NON_NULL)
public class StatisticsDto {

    Integer year;

    String month;

    String status;

    Integer count;

}
