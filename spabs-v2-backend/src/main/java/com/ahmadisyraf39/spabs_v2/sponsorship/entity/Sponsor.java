package com.ahmadisyraf39.spabs_v2.sponsorship.entity;

import com.ahmadisyraf39.spabs_v2.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "sponsors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sponsor extends BaseEntity {

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String contactPerson;

    private String phoneNumber;

    private String email;

    private String address;
}
