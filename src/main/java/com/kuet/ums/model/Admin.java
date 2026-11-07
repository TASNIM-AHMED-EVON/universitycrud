package com.kuet.ums.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends AppUser {

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
