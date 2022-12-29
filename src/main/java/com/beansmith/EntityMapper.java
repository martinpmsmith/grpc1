package com.beansmith;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Component
public class EntityMapper {

    public static String camelToSnake(String str) {
        // Regular Expression
        String regex = "([a-z])([A-Z]+)";
        // Replacement string
        String replacement = "$1_$2";
        // Replace the given regex
        // with replacement string
        // and convert it to lower case.
        return str.replaceAll(regex, replacement).toLowerCase();
    }

    public static Map<String, String> updateQueriesForEntityBase(EntityBase source) {
        Map<String, String> retval = new HashMap<>();
        BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrappedSource.getPropertyDescriptors();
        for (String table : source.getTableColumnMap().keySet()) {
            List<String> pkCols = source.getKeyColumns(table);
            List<String> columns = source.getTableColumnMap().get(table);
            String query = "update ~table~ set ~updates~ where ~whereclause~";
            StringBuilder updates = new StringBuilder();
            for (String col : columns) {
                if (pkCols.contains(col)) {
                    continue;
                }
                Object value = wrappedSource.getPropertyValue(source.camelColumnFromSnake(col));
                boolean wrap = wrapColumnData(value);
                if (updates.length() > 0) {
                    updates.append(", ");
                }
                updates.append(col)
                        .append(" = ")
                        .append(wrap ? "'" : "");
                appendValue(updates, value);
                updates.append(wrap ? "'" : "");
            }
            StringBuilder where = new StringBuilder();
            for (String col : pkCols) {
                if (where.length() > 0) {
                    where.append(" ");
                }
                Object value = wrappedSource.getPropertyValue(source.camelColumnFromSnake(col));
                boolean wrap = wrapColumnData(value);
                where.append(col).append(" = ").append(wrap ? "'" : "");
                appendValue(where, value);
                where.append(wrap ? "'" : "");
            }
            query = query.replace("~updates~", updates.toString());
            query = query.replace("~whereclause~", where.toString());
            query = query.replace("~table~", table);
            retval.put(table, query);
        }
        return retval;
    }

    public static List<EntityBase> entityBaseListFromQueryResult(Class clazz, List<Map<String, Object>> rows) {
        if (!EntityBase.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + "must extend EdmEntityBase");
        }
        List<EntityBase> entities = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            final BeanWrapper wrappedTarget = new BeanWrapperImpl(clazz);
            EntityBase target = ((EntityBase) wrappedTarget.getWrappedInstance());
            for (String key : row.keySet()) {
                String targetProperty = target.camelColumnFromSnake(key);
                if (wrappedTarget.isWritableProperty(targetProperty)) {
                    // write custom types
                    wrappedTarget.setPropertyValue(targetProperty, row.get(key));
                }
            }
            entities.add((EntityBase) wrappedTarget.getWrappedInstance());
        }
        return entities;
    }


    public static Map<String, String> insertQueriesForEntityBase(EntityBase source) {
        Map<String, String> retval = new HashMap<>();
        BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrappedSource.getPropertyDescriptors();
        for (String table : source.getTableColumnMap().keySet()) {
            List<String> columns = source.getTableColumnMap().get(table);
            String query = "insert into ~table~ (~columns~) values (~values~)";
            StringBuilder colString = new StringBuilder();
            StringBuilder valString = new StringBuilder();
            for (String col : columns) {
                Object value = wrappedSource.getPropertyValue(source.camelColumnFromSnake(col));
                boolean wrap = wrapColumnData(value);
                if (colString.length() > 0) {
                    colString.append(", ");
                    valString.append(", ");
                }
                colString.append(col);
                valString.append(wrap ? "'" : "");
                appendValue(valString, value);
                valString.append(wrap ? "'" : "");
            }
            query = query.replace("~columns~", colString.toString());
            query = query.replace("~values~", valString.toString());
            query = query.replace("~table~", table);
            retval.put(table, query);
        }
        return retval;
    }

    private static void appendValue(StringBuilder valString, Object value) {
        if (value instanceof String) {
            valString.append(((String) value).replace("'", "''"));
        } else if (value instanceof Boolean) {
            valString.append("cast(").append(((Boolean) value) ? " 1 " : " 0 ").append(" as BIT ) ");
        } else if (value == null) {
            valString.append("NULL");
        } else {
            valString.append(value);
        }
    }

    private static boolean wrapColumnData(Object value) {
        if (value == null) {
            return false;
        }
        String classname = value.getClass().getSimpleName();
        switch (classname) {
            case "Boolean":
            case "Integer":
            case "Long":
            case "Double":
            case "Float":
                return false;
            case "ByteString":
            case "String":
                return true;
            default:
                return true;
        }

    }

    @NotNull
    @SneakyThrows
    public static EntityBase protoToEntityBase(Message source, Class clazz) {
        if (!EntityBase.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + "must extend EdmEntityBase");
        }
        final BeanWrapper wrappedTarget = new BeanWrapperImpl(clazz);
        BeanWrapper wrappedTargetwrappedSource = new BeanWrapperImpl(source);
        Set<Descriptors.FieldDescriptor> keys = source.getAllFields().keySet();
        for (Descriptors.FieldDescriptor fd : keys) {
            if (fd.getName().equals("kvp") || fd.getName().equals("child")) {
                continue;
            }
            String name = translatePropertyName(fd.getName());
            setTargetProperty(wrappedTarget, name, source.getField(fd));
        }
        Descriptors.FieldDescriptor kvpFd = source.getDescriptorForType().findFieldByName("kvp");
        int kvpFields = source.getRepeatedFieldCount(kvpFd);

        for (int i = 0; i < kvpFields; i++) {
            Hello.KevValuePair kvp = (Hello.KevValuePair) source.getRepeatedField(kvpFd, i);
            Hello.DataValue dv = kvp.getValue();
            Hello.DataValue.ValueCase vc = dv.getValueCase();
            switch (vc) {
                case INT_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getIntValue());
                    break;
                case BOOL_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getBoolValue());
                    break;
                case LONG_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getLongValue());
                    break;
                case FLOAT_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getFloatValue());
                    break;
                case DOUBLE_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getDoubleValue());
                    break;
                case STRING_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getStringValue());
                    break;
                case BYTES_VALUE:
                    setTargetProperty(wrappedTarget, kvp.getKey(), dv.getBytesValue());
                    break;
            }
        }
        return (EntityBase) wrappedTarget.getWrappedInstance();
    }

    private static String translatePropertyName(String name) {
        String propertyName = StringUtils.uncapitalize(name);
        if (propertyName.contains("_")) {
            propertyName = CaseUtils.toCamelCase(propertyName, false, '_');
        }
        return propertyName;
    }

    private static void setTargetProperty(@NotNull BeanWrapper wrappedTarget, String name, Object value) {
        if (wrappedTarget.isWritableProperty(name)) {
            wrappedTarget.setPropertyValue(name, value);
        }
    }

    public static Message pojoToProto(Object source, Class clazz) throws NoSuchMethodException, ClassNotFoundException {
        GeneratedMessageV3.Builder<?> builder = null;
        try {
            builder = (GeneratedMessageV3.Builder<?>) clazz.getDeclaredMethod("newBuilder").invoke(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        Descriptors.FieldDescriptor kvpFd = builder.getDescriptorForType().findFieldByName("kvp");

        final BeanWrapper wrappedSource = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = wrappedSource.getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            String propertyName = pd.getName();
            TypeDescriptor type = wrappedSource.getPropertyTypeDescriptor(propertyName);
            Descriptors.FieldDescriptor nameFd = getFieldDescriptor(builder, propertyName);
            Hello.KevValuePair.Builder kvpBuilder = Hello.KevValuePair.newBuilder();
            Object value = wrappedSource.getPropertyValue(propertyName);
            // only send properties that have values and are writeable.
            if (value != null && wrappedSource.isWritableProperty(propertyName)) {
                if (nameFd != null) {
                    builder.setField(nameFd, value);
                } else {
                    Descriptors.FieldDescriptor keyFd = kvpBuilder.getDescriptorForType().findFieldByName("key");
                    Descriptors.FieldDescriptor valueFd = kvpBuilder.getDescriptorForType().findFieldByName("value");
                    Hello.DataValue.Builder dvBuilder = Hello.DataValue.newBuilder();
                    String classname = Class.forName(pd.getPropertyType().getName()).getSimpleName();
                    switch (classname) {
                        case "Boolean":
                            dvBuilder.setBoolValue((Boolean) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "Integer":
                            dvBuilder.setIntValue((Integer) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "Long":
                            dvBuilder.setLongValue((Long) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "Double":
                            dvBuilder.setDoubleValue((Double) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "Float":
                            dvBuilder.setFloatValue((Float) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "ByteString":
                            dvBuilder.setBytesValue((ByteString) wrappedSource.getPropertyValue(propertyName));
                            break;
                        case "String":
                            dvBuilder.setStringValue((String) wrappedSource.getPropertyValue(propertyName));
                            break;
                        default:
                            dvBuilder.setStringValue(Objects.requireNonNull(wrappedSource
                                    .getPropertyTypeDescriptor(propertyName)).toString());
                            break;

                    }
                    kvpBuilder.setField(valueFd, dvBuilder.build());
                    kvpBuilder.setField(keyFd, propertyName);
                    builder.addRepeatedField(kvpFd, kvpBuilder.build());

                }
            }
        }
        return builder.build();
    }

    private static Descriptors.FieldDescriptor getFieldDescriptor(GeneratedMessageV3.Builder<?> builder, String propertyName) {
        Descriptors.FieldDescriptor nameFd = builder.getDescriptorForType().findFieldByName(propertyName);
        if (nameFd == null) {
            nameFd = builder.getDescriptorForType().findFieldByName(StringUtils.capitalize(propertyName));
        }
        if (nameFd == null) {
            nameFd = builder.getDescriptorForType().findFieldByName(camelToSnake(propertyName));
        }
        return nameFd;
    }

}
