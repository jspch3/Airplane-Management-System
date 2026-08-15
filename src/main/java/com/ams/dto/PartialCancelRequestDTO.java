package com.ams.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PartialCancelRequestDTO {

    @NotEmpty(message = "At least one passenger ID must be selected for cancellation")
    private List<Long> passengerIds;
}
