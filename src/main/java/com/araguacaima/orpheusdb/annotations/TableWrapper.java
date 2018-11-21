package com.araguacaima.orpheusdb.annotations;

public class TableWrapper {


    public static javax.persistence.Table toPersistenceTable(Table table_) {
        return (javax.persistence.Table) table_;
    }

    public static Table fromPersistenceTable(javax.persistence.Table table_) {
        Table table = new Table();
        table.setName(table_.name());
        table.setCatalog(table_.catalog());
        table.setSchema(table_.schema());
        table.setUniqueConstraints(table_.uniqueConstraints());
        table.setIndexes(table_.indexes());
        return table;
    }
}
