package com.beansmith;

import lombok.AccessLevel;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityBase {

    @Setter(AccessLevel.NONE)
    private List<String> oneToManyTables = new ArrayList<>();
    @Setter(AccessLevel.NONE)
    private Map<String, List<String>> childColumns = new HashMap<>();
    public List<String> getOneToManyTables() {
        return java.util.Collections.unmodifiableList(oneToManyTables);
    }

    public Map<String, List<String>> getChildColumns() {
        return java.util.Collections.unmodifiableMap(childColumns);
    }

    public void addOneToManyTable(String table){
        oneToManyTables.add(table);
    }

    public void addChildColumnToTable(String table, String column){
        childColumns.putIfAbsent(table, new ArrayList<>());
        childColumns.get(table).add(column);
    }
}
