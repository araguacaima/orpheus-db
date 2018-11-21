package com.araguacaima.orpheusdb;


import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PersistenceUnit;
import javax.persistence.Table;

@PersistenceUnit(unitName = "open-archi")
@Entity
@Table(schema = "META", name = "Indexable")
@DynamicUpdate
public class Indexable extends BaseEntity {

    @Type(type = "int-array")
    @Column(
            name = "rlist",
            columnDefinition = "integer[]"
    )
    private Integer[] rlist;

    public Indexable() {
        super();
    }

    public Integer[] getRlist() {
        return rlist;
    }

    public void setRlist(Integer[] rlist) {
        this.rlist = rlist;
    }

}
