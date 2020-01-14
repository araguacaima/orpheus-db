package com.araguacaima.orpheusdb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@MappedSuperclass
@PersistenceUnit(unitName = "orpheus-db-test")
public abstract class F implements Serializable {

    @Id
    @NotNull
    @Column(name = "Id")
    private String id;

    public F() {
        this.id = generateId();
    }

    @JsonIgnore
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

}