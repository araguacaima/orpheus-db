package com.araguacaima.orpheusdb;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table
public class Versionable extends BaseEntity {

    @Column
    private String author;

    @Column
    private int num_records = 0;

/*    @Type(type = "int-array")
    @Column(
            name = "parent",
            columnDefinition = "integer[]"
    )*/
    private Integer[] parent;

/*    @Type(type = "int-array")
    @Column(
            name = "children",
            columnDefinition = "integer[]"
    )*/
    private Integer[] children;

    @Column
    private DateTime create_time;

    @Column
    private DateTime commit_time;

    @Column
    private String commit_msg;

    public Versionable() {
        super();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getNum_records() {
        return num_records;
    }

    public void setNum_records(int num_records) {
        this.num_records = num_records;
    }

    public Integer[] getParent() {
        return parent;
    }

    public void setParent(Integer[] parent) {
        this.parent = parent;
    }

    public Integer[] getChildren() {
        return children;
    }

    public void setChildren(Integer[] children) {
        this.children = children;
    }

    public DateTime getCreate_time() {
        return create_time;
    }

    public void setCreate_time(DateTime create_time) {
        this.create_time = create_time;
    }

    public DateTime getCommit_time() {
        return commit_time;
    }

    public void setCommit_time(DateTime commit_time) {
        this.commit_time = commit_time;
    }

    public String getCommit_msg() {
        return commit_msg;
    }

    public void setCommit_msg(String commit_msg) {
        this.commit_msg = commit_msg;
    }
}
