package com.araguacaima.orpheusdb;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class Indexable extends BaseEntity {

    /*
        @Type(type = "int-array")
        @Column(
                name = "rlist",
                columnDefinition = "integer[]"
        )
    */
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
