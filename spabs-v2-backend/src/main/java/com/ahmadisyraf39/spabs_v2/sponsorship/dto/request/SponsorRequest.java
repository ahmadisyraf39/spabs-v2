package com.ahmadisyraf39.spabs_v2.sponsorship.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SponsorRequest {

    @NotBlank
    private String name;

    private String contactPerson;

    private String phoneNumber;

    private String email;

    private String address;
}
