package com.technofacts.lnf.dto.client;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

import com.technofacts.lnf.client.model.Project;
import lombok.*;

@ToString
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {

    private UUID id;

    @NotNull(message = "name cannot be null")
    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "type cannot be null")
    @NotBlank(message = "type cannot be blank")
    private String type;

    @NotNull(message = "status cannot be null")
    @NotBlank(message = "status cannot be blank")
    private String status;

    @NotNull(message = "description cannot be null")
    @NotBlank(message = "description cannot be blank")
    private String description;

    @NotNull(message = "startDate cannot be null")
    @NotBlank(message = "startDate cannot be blank")
    private LocalDate startDate;

    private LocalDate endDate;

}
