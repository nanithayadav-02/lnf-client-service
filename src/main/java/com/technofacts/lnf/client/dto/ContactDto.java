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
public class ContactDto {

    private UUID id;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "phoneNumber cannot be null")
    @NotBlank(message = "phoneNumber cannot be blank")
    private String phoneNumber;

    @NotNull(message = "email cannot be null")
    @NotBlank(message = "email cannot be blank")
    private String email;

    @NotNull(message = "designation cannot be null")
    @NotBlank(message = "designation cannot be blank")
    private String designation;

    @NotNull(message = "department cannot be null")
    @NotBlank(message = "department cannot be blank")
    private String department;
    
}
