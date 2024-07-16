package com.technofacts.lnf.client.model;

import com.technofacts.lnf.client.model.enums.AddressType;
import com.technofacts.lnf.model.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.*;

@ToString
@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@AllArgsConstructor
public class Address extends AuditableEntity {

    @Column(nullable = false, name = "address_text")
    private String addressText;

    @Column(nullable = true, name = "town")
    private String town;

    @Column(nullable = false, name = "city")
    private String city;

    @Column(nullable = false, name = "state")
    private String state;

    @Column(nullable = false, name = "post_code")
    private String postCode;

    @Column(nullable = false, name = "country")
    private String country;

    @Column(name = "addressType")
    @Enumerated(EnumType.STRING)
    private AddressType addressType;
}
