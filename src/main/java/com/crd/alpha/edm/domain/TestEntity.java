package com.crd.alpha.edm.domain;


import com.google.protobuf.ByteString;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = true)
public class TestEntity extends EntityBase {
    private Long primaryKey;
    private Boolean boolVal;
    private Float floatVal;
    private Integer intVal;
    private Double doubleVal;
    private Long longVal;
    private String stringVal;
    private ByteString bytesVal;
    private String thisWasHere;
    private Long soWasI;
//    private Timestamp timestamp;
    @Override
    public synchronized void setTableColumnMap() {
        if (this.name == null) {
            this.name = "entity_test";
            addTableColumn(this.name, EntityMapper.camelToSnake("boolVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("floatVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("intVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("doubleVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("longVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("stringVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("bytesVal"));
            addTableColumn(this.name, EntityMapper.camelToSnake("thisWasHere"));
            addTableColumn(this.name, EntityMapper.camelToSnake("soWasI"));
            addTableColumn(this.name, EntityMapper.camelToSnake("primaryKey"));
            addTablePrimaryKeyColumn(this.name, "primary_key");
        }
    }

}
