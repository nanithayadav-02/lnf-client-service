package com.technofacts.lnf.client.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

import lombok.*;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstDto {

    private UUID id;

    @NotNull(message = "location cannot be null")
    @NotBlank(message = "location cannot be blank")
    private String location;

    @NotNull(message = "number cannot be null")
    @NotBlank(message = "number cannot be blank")
    private String number;

}
