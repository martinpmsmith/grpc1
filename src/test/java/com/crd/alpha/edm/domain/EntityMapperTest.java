package com.crd.alpha.edm.domain;

import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EntityMapperTest {


    @Test
    void updateQueriesForEntityBase() {
        TestEntity te = getTestEntity1();
        Map<String, String> expected = new HashMap<>();
        expected.put("entity_test", getUpdateQuery());

        Map<String, String> actual = EntityMapper.updateQueriesForEntityBase(te);

        assertThat(actual.equals(expected));

    }

    @Test
    void entityBaseListFromQueryResult() {
        List<Map<String, Object>> rows = getDbRows();
        List<TestEntity> expected = new ArrayList<>();
        expected.add(getTestEntity1());
        expected.add(getTestEntity2());

        List<EntityBase> actual = EntityMapper.entityBaseListFromQueryResult(TestEntity.class, rows);

        assertThat(actual.equals(expected));
    }

    @Test
    void insertQueriesForEntityBase() {
        TestEntity te = getTestEntity1();
        Map<String, String> expected = new HashMap<>();
        expected.put("entity_test", getInsertQuery());

        Map<String, String> actual = EntityMapper.insertQueriesForEntityBase(te);

        assertThat(actual.equals(expected));
    }

    @Test
    void protoToEntityBase() {
        Domain.TestEntity ms = getProto1();
        TestEntity expected = getTestEntity1();

        EntityBase actual = EntityMapper.protoToEntityBase(ms, TestEntity.class);

        assertThat(actual.equals(expected));
    }


    @Test
    void entityBaseListFromQueryResult_throws_for_non_EntityBase() {
        Domain.TestEntity ms = getProto1();

        assertThatThrownBy(() -> {
            EntityMapper.entityBaseListFromQueryResult(Object.class, getDbRows());
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must extend EdmEntityBase");
    }

    @Test
    void proto_to_entity_basethrows_for_non_EntityBase() {
        Domain.TestEntity ms = getProto1();

        assertThatThrownBy(() -> {
            EntityMapper.protoToEntityBase(ms, Object.class);
        }).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must extend EdmEntityBase");
    }

    @Test
    void pojoToProto() throws ClassNotFoundException, NoSuchMethodException {
        Domain.TestEntity expected = getProto1();
        TestEntity te = getTestEntity1();

        Message actual = EntityMapper.pojoToProto(te, Domain.TestEntity.class);

        assertThat(actual.equals(expected));
    }


    private Domain.TestEntity getProto1() {
        Domain.TestEntity.Builder builder = Domain.TestEntity.newBuilder();
        List<Entity.KevValuePair> kvps = new ArrayList<>();
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("boolVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setBoolValue(true)).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("doubleVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setDoubleValue(12.2323)).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("floatVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setFloatValue(12.123f)).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("intVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setIntValue(12)).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("longVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setLongValue(12L)).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("name")
                .setValue(Entity.DataValue.newBuilder()
                        .setStringValue("entity_test")).build());
        kvps.add(Entity.KevValuePair.newBuilder()
                .setKey("stringVal")
                .setValue(Entity.DataValue.newBuilder()
                        .setStringValue("this is a string")).build());

        builder.setPrimaryKey(12L)
                .setThisWasHere("existing value")
                .setSoWasI(1234L)
                .setTableName("entity_test").build();
        builder.addAllKvp(kvps);
        return builder.build();
    }

    private TestEntity getTestEntity1() {
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

    private TestEntity getTestEntity2() {
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

    private List<Map<String, Object>> getDbRows() {
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

    private String getInsertQuery() {
        return "insert into entity_test (bool_val, float_val, int_val, double_val, long_val, " +
                "string_val, bytes_val, this_was_here, so_was_i, primary_key) values (cast( 1  as BIT ) " +
                ", 12.123, 12, 12.2323, 12, 'this is a string', NULL, 'existing value', 1234, 12)";
    }

    private String getUpdateQuery() {
        return "update entity_test set bool_val = cast( 1  as BIT ) , float_val = 12.123, int_val = 12," +
                " double_val = 12.2323, long_val = 12, string_val = 'this is a string', bytes_val = NULL," +
                " this_was_here = 'existing value', so_was_i = 1234 where primary_key = 12";
    }
}