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
public class EscalationDto {

    private UUID id;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "email cannot be null")
    @NotBlank(message = "email cannot be blank")
    private String email;

    @NotNull(message = "mobileNumber cannot be null")
    @NotBlank(message = "mobileNumber cannot be blank")
    private String mobileNumber;

    private String phoneNumber;

}
