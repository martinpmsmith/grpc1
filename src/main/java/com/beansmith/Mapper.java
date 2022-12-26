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

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.Set;

public class Mapper {

    public static String camelToSnake(String str) {
        // Regular Expression
        String regex = "([a-z])([A-Z]+)";
        // Replacement string
        String replacement = "$1_$2";
        // Replace the given regex
        // with replacement string
        // and convert it to lower case.
        str = str.replaceAll(regex, replacement).toLowerCase();
        // return string
        return str;
    }

    @NotNull
    @SneakyThrows
    public static EntityBase protoToEntityBase(Message source, Class clazz) {
        if (!EntityBase.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + "must extend EdmEntityBase");
        }
        final BeanWrapper wrappedTarget = new BeanWrapperImpl(clazz);
        BeanWrapper wrappedSource = new BeanWrapperImpl(source);
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
            setTargetProperty(wrappedSource, kvp.getKey(), kvp.getValue());
        }
        return (EntityBase) wrappedSource.getWrappedInstance();
    }

    private static String translatePropertyName(String name) {
        String propertyName = StringUtils.uncapitalize(name);
        if (propertyName.contentEquals("_")) {
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
