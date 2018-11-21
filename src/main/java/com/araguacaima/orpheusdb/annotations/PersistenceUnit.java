package com.araguacaima.orpheusdb.annotations;

import java.lang.annotation.Annotation;

public class PersistenceUnit implements javax.persistence.PersistenceUnit {

    /**
     * (Optional) The name by which the entity manager factory is to be accessed
     * in the environment referencing context;  not needed when
     * dependency injection is used.
     */
    private String name = "";

    /**
     * (Optional) The name of the persistence unit as defined in the
     * <code>persistence.xml</code> file. If specified, the
     * persistence unit for the entity manager factory that is
     * accessible in JNDI must have the same name.
     */
    private String unitName = "";


    @Override
    public String name() {
        return name;
    }

    @Override
    public String unitName() {
        return unitName;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }


    @Override
    public Class<? extends Annotation> annotationType() {
        return PersistenceUnit.class;
    }
}
