package com.araguacaima.orpheusdb.model;


import com.araguacaima.orpheusdb.model.versionable.C;

import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;

@Entity
@PersistenceUnit(unitName = "orpheus-db-test")
public abstract class B extends C {
    public B() {
    }
}
