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
public class ClientDto {

    private UUID id;

    @NotNull(message = "clientId cannot be null")
    @NotBlank(message = "clientId cannot be blank")
    private String clientId;

}
