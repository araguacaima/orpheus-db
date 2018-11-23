package com.araguacaima.orpheusdb.model.versionable;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@PersistenceUnit(unitName = "orpheus-db-test")
public abstract class C extends D {

    @Column
    private String testField3;

    @ElementCollection
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    @CollectionTable(schema = "orpheusdb", name = "c_testField4", joinColumns = @JoinColumn(name = "testField4_Id"))
    private Map<String, String> testField4 = new HashMap<>();

    public C() {
    }

    public String getTestField3() {
        return testField3;
    }

    public void setTestField3(String testField3) {
        this.testField3 = testField3;
    }

    public Map<String, String> getTestField4() {
        return testField4;
    }

    public void setTestField4(Map<String, String> testField4) {
        this.testField4 = testField4;
    }
}