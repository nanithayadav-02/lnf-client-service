package com.technofacts.lnf.dto.client;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @NotNull(message = "code cannot be null")
    @NotBlank(message = "code cannot be blank")
    private String code;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "pan cannot be null")
    @NotBlank(message = "pan cannot be blank")
    private String pan;

    @NotNull(message = "workingFrom cannot be null")
    @NotBlank(message = "workingFrom cannot be blank")
    private LocalDate workingFrom;

    @NotNull(message = "agreementExpiryDate cannot be null")
    @NotBlank(message = "agreementExpiryDate cannot be blank")
    private LocalDate agreementExpiryDate;

    @NotNull(message = "clientDetails cannot be null")
    @NotBlank(message = "clientDetails cannot be blank")
    private String clientDetails;

    private AddressDto address = null;

    private List<ContactDto> contacts = new ArrayList<>();

    private EscalationDto escalation = null;

    private List<GstDto> gst = new ArrayList<>();

    private DocumentDto agreement = null;

    private DocumentDto clientLogo = null;

}
