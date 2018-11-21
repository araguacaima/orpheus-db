package com.araguacaima.orpheusdb.annotations;

import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import java.lang.annotation.Annotation;

public class Table implements javax.persistence.Table {

    /**
     * (Optional) The name of the table.
     * <p> Defaults to the entity name.
     */
    private String name = "";

    /**
     * (Optional) The catalog of the table.
     * <p> Defaults to the default catalog.
     */
    private String catalog = "";

    /**
     * (Optional) The schema of the table.
     * <p> Defaults to the default schema for user.
     */
    private String schema = "";

    /**
     * (Optional) Unique constraints that are to be placed on
     * the table. These are only used if table generation is in
     * effect. These constraints apply in addition to any constraints
     * specified by the <code>Column</code> and <code>JoinColumn</code>
     * annotations and constraints entailed by primary key mappings.
     * <p> Defaults to no additional constraints.
     */
    private UniqueConstraint[] uniqueConstraints = new UniqueConstraint[0];

    /**
     * (Optional) Indexes for the table.  These are only used if
     * table generation is in effect.  Note that it is not necessary
     * to specify an index for a primary key, as the primary key
     * index will be created automatically.
     *
     * @since Java Persistence 2.1
     */
    private Index[] indexes = new Index[0];


    @Override
    public String name() {
        return name;
    }

    @Override
    public String catalog() {
        return catalog;
    }

    @Override
    public String schema() {
        return schema;
    }

    @Override
    public UniqueConstraint[] uniqueConstraints() {
        return uniqueConstraints;
    }

    @Override
    public Index[] indexes() {
        return indexes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public void setUniqueConstraints(UniqueConstraint[] uniqueConstraints) {
        this.uniqueConstraints = uniqueConstraints;
    }

    public void setIndexes(Index[] indexes) {
        this.indexes = indexes;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Table.class;
    }
}
