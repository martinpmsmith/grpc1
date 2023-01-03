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

    /**
     * enotuty name. this is usully the root table for the enotity e.g. EDM_Issuer
     */
    protected String name;

    /**
     * list of ny one to many tables associated with the entity.
     * all one ot many table will have an associated List<EntityType> property
     * in the entity.
     */
    private final List<String> oneToManyTables = new ArrayList<>();

    /**
     * associates the tables involved in creating this entity with column names
     * used in the entoty
     */
    private final Map<String, List<String>> tableColumnMap = new HashMap<>();
    /*
    look into need to handle new roperties dynamically.
    private final Map<String, Object> addedProperties = new HashMap<>();
*/
    /**
     * maps primary key columns to all tables involved in creating this entoty
     */
    private final Map<String, List<String>> tablePrimryKeyColumnMap = new HashMap<>();

    /**
     * map of entity properties to their associated database column
     */
    private static final Map<String, String> columnsCamelToSnakeMap = new HashMap<>();
    /**
     * map of database columns to theie associated entity property.
     */
    private static final Map<String, String> columnsSnakeToCamelMap = new HashMap<>();

    /**
     * calls setTableColumnMap method to instantiate core data.
     */
    public EntityBase() {
        setTableColumnMap();
    }

    public void setName(String name){
        this.name = name;
        setTableColumnMap();
    }

    /**
     * this method must be overrideen to correctly set up the core data for database persistence
     * if should set the entity name and add data using addTableColumn
     */
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

    /**
     * adds a database column to columnsCamelToSnakeMap and columnsSnakeToCamelMap
     * using the default toCammelCase method from CaseUtils. If an override is required
     * the addOverridetoCaseMaps method must be called after this method is called for the
     * relevant column.
     * @param column
     */
    private void addToCaseMaps(String column) {
        String camel = CaseUtils.toCamelCase(column,false,'_');
        columnsCamelToSnakeMap.putIfAbsent(camel, column);
        columnsSnakeToCamelMap.putIfAbsent(column, camel);
    }

    /**
     * overrides the default snake to camel mapping with a custom value.
     * the existing default mapping will be removed from columnsCamelToSnakeMap
     * and columnsSnakeToCamelMap.
     *
     * @param snakeColumn snake case column name.
     * @param camelColumn override value.
     */
    private void addOverridetoCaseMaps(String snakeColumn, String camelColumn){
        String toRemove = CaseUtils.toCamelCase(camelColumn,false,'_');
        columnsCamelToSnakeMap.remove(toRemove);
        columnsCamelToSnakeMap.putIfAbsent(camelColumn, snakeColumn);
        columnsSnakeToCamelMap.putIfAbsent(snakeColumn, camelColumn);

    }

    public String snakeColumnFromCamel(String camel){
        return columnsCamelToSnakeMap.get(camel);
    }
    public String camelColumnFromSnake(String snake){
        return columnsSnakeToCamelMap.get(snake);
    }

    /***
     * returns the primary key columns for the table.
     *
     * @param table
     * @return
     */
    public List<String> getKeyColumns(String table){
        return tablePrimryKeyColumnMap.get(table);
    }
}
