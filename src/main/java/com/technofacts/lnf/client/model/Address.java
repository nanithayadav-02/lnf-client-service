package com.technofacts.lnf.client.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.technofacts.lnf.model.AuditableEntity;
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

}
