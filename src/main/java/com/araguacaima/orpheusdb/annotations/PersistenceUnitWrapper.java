package com.araguacaima.orpheusdb.annotations;


public class PersistenceUnitWrapper {


    public static javax.persistence.PersistenceUnit toPersistenceTable(PersistenceUnit persistenceUnit_) {
        return (javax.persistence.PersistenceUnit) persistenceUnit_;
    }

    public static PersistenceUnit fromPersistenceTable(javax.persistence.PersistenceUnit persistenceUnit_) {
        PersistenceUnit persistenceUnit = new PersistenceUnit();
        persistenceUnit.setName(persistenceUnit_.name());
        persistenceUnit.setUnitName(persistenceUnit_.unitName());
        return persistenceUnit;
    }
}
