package com.araguacaima.orpheusdb;


import com.araguacaima.orpheusdb.helpers.hibernate.IntArrayType;
import com.araguacaima.orpheusdb.helpers.hibernate.StringArrayType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.util.UUID;

@TypeDefs({
        @TypeDef(
                name = "string-array",
                typeClass = StringArrayType.class
        ),
        @TypeDef(
                name = "int-array",
                typeClass = IntArrayType.class
        )
})
@MappedSuperclass
public class BaseEntity {

    public static final String HEAD_VERSION = "HEAD";

    @Id
    private String vid = HEAD_VERSION;

    public BaseEntity() {
        this.vid = generateId();
    }

    @JsonIgnore
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public String getVid() {
        return vid;
    }

    public void setVid(String vid) {
        this.vid = vid;
    }

}