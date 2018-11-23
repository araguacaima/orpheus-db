package com.araguacaima.orpheusdb.model;

import org.springframework.stereotype.Component;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@PersistenceUnit(unitName = "orpheus-db-test")
@Table(name = "F", schema = "orpheusdb", uniqueConstraints = @UniqueConstraint(columnNames = {"testField7", "testField9"}))
@DiscriminatorColumn(name = "modelType", discriminatorType = DiscriminatorType.STRING)
@Component
public abstract class E extends F {

    @Column(nullable = false)
    @NotNull
    private String testField7;

    @ElementCollection
    @CollectionTable(name = "Tags", schema = "orpheusdb")
    private Set<String> testField8 = new LinkedHashSet<>();

    @Column(nullable = false)
    @NotNull
    private String testField9;

    public E() {
    }

    public String getTestField7() {
        return testField7;
    }

    public void setTestField7(String testField7) {
        this.testField7 = testField7;
    }

    public Set<String> getTestField8() {
        return testField8;
    }

    public void setTestField8(Set<String> testField8) {
        this.testField8 = testField8;
    }

    public String getTestField9() {
        return testField9;
    }

    public void setTestField9(String testField9) {
        this.testField9 = testField9;
    }
}
