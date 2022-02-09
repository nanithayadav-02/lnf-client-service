package com.technofacts.lnf.dto.client;

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
public class AddressDto {

    private UUID id;

    @NotNull(message = "addressText cannot be null")
    @NotBlank(message = "addressText cannot be blank")
    private String addressText;

    @NotNull(message = "town cannot be null")
    @NotBlank(message = "town cannot be blank")
    private String town;

    @NotNull(message = "city cannot be null")
    @NotBlank(message = "city cannot be blank")
    private String city;

    @NotNull(message = "state cannot be null")
    @NotBlank(message = "state cannot be blank")
    private String state;

    @NotNull(message = "postCode cannot be null")
    @NotBlank(message = "postCode cannot be blank")
    private String postCode;

    @NotNull(message = "country cannot be null")
    @NotBlank(message = "country cannot be blank")
    private String country;

}
