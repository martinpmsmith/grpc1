package com.beansmith;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EntityMapperTest {

    @Test
    void camelToSnake() {
    }

    @Test
    void updateQueriesForEntityBase() {
    }

    @Test
    void entityBaseListFromQueryResult() {
    }

    @Test
    void insertQueriesForEntityBase() {
    }

    @Test
    void protoToEntityBase() {
    }

    @Test
    void pojoToProto() {
    }


    private Hello.MapperSample getProto1() {
        return null;
    }
    private TestEntity getTestEntity1()
    {
        TestEntity mp = new TestEntity();
        mp.setBoolVal(true);
        mp.setPrimaryKey(12L);
        mp.setDoubleVal(12.2323);
        mp.setFloatVal(12.123f);
        mp.setIntVal(12);
        mp.setThisWasHere("existing value");
        mp.setLongVal(12L);
        mp.setSoWasI(1234L);
        mp.setStringVal("this is a string");

        return mp;
    }
    private TestEntity getTestEntity2()
    {
        TestEntity mp = new TestEntity();
        mp.setBoolVal(false);
        mp.setPrimaryKey(14L);
        mp.setDoubleVal(null);
        mp.setFloatVal(213.123f);
        mp.setIntVal(12333);
        mp.setThisWasHere(null);
        mp.setLongVal(12L);
        mp.setSoWasI(1234L);
        mp.setStringVal("");
        return mp;
    }

    private List<Map<String,Object>> getDbRows()
    {
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("primary_key", 12L);
        row.put("bool_val", true);
        row.put("double_val", 12.2323);
        row.put("float_val", 12.123f);
        row.put("int_val", 12);
        row.put("this_was_here", "existing value");
        row.put("so_was_i", 1234L);
        row.put("long_val", 12L);
        row.put("string_val", "this is a string");
        Map<String, Object> row2 = new HashMap<>();
        row2.put("primary_key", 14L);
        row2.put("bool_val", false);
        row2.put("double_val", null);
        row2.put("float_val", 213.123f);
        row2.put("int_val", 12333);
        row2.put("this_was_here", null);
        row2.put("so_was_i", 1234L);
        row2.put("long_val", 12L);
        row2.put("string_val", "");
        rows.add(row);
        rows.add(row2);

        return rows;
    }
}