package com.crd.alpha.edm.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.text.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EqualsAndHashCode
@Getter
public abstract class EntityBase {

    protected String name;
    private final List<String> oneToManyTables = new ArrayList<>();
    private final Map<String, List<String>> tableColumnMap = new HashMap<>();
    /*
    look into need to handle new roperties dynamically.
    private final Map<String, Object> addedProperties = new HashMap<>();
*/
    private final Map<String, List<String>> tablePrimryKeyColumnMap = new HashMap<>();

    private static final Map<String, String> columnsCamelToSnakeMap = new HashMap<>();
    private static final Map<String, String> columnsSnakeToCamelMap = new HashMap<>();

    public EntityBase() {
        setTableColumnMap();
    }

    public void setName(String name){
        setTableColumnMap();
    }
    protected abstract void setTableColumnMap();
    public List<String> getOneToManyTables() {
        return java.util.Collections.unmodifiableList(oneToManyTables);
    }

    public Map<String, List<String>> getTableColumnMap() {
        return java.util.Collections.unmodifiableMap(tableColumnMap);
    }

    public void addOneToManyTable(String table){
        oneToManyTables.add(table);
    }

    public void addTableColumn(String table, String column){
        addToCaseMaps(column);
        tableColumnMap.putIfAbsent(table, new ArrayList<>());
        tableColumnMap.get(table).add(column);
    }
    public void addTablePrimaryKeyColumn(String table, String column){
        tablePrimryKeyColumnMap.putIfAbsent(table, new ArrayList<>());
        tablePrimryKeyColumnMap.get(table).add(column);
    }

    private void addToCaseMaps(String column) {
        String camel = CaseUtils.toCamelCase(column,false,'_');
        columnsCamelToSnakeMap.putIfAbsent(camel, column);
        columnsSnakeToCamelMap.putIfAbsent(column, camel);
    }

    public String snakeColumnFromCamel(String camel){
        return columnsCamelToSnakeMap.get(camel);
    }
    public String camelColumnFromSnake(String snake){
        return columnsSnakeToCamelMap.get(snake);
    }

    public List<String> getKeyColumns(String table){
        return tablePrimryKeyColumnMap.get(table);
    }
}
