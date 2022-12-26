package com.beansmith;


import com.google.protobuf.ByteString;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class MapperEntity extends EntityBase {
    Boolean  boolVal;
    Float floatVal;
    Integer intVal;
    Double  doubleVal;
    Long longVal;
    String stringVal;
    ByteString bytesVal;

    String thisWasHere;
    Long  soWasI;

    public void run(java.lang.String... args) throws Exception {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from actor");
        for (Map<String, Object> row : rows){
            System.out.println(" orw  " + row.get("actor_id") + " " + row.get("first_name"));
        }

    }
}
