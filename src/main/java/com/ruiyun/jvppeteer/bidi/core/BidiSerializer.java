package com.ruiyun.jvppeteer.bidi.core;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ruiyun.jvppeteer.bidi.entities.LocalValue;
import com.ruiyun.jvppeteer.common.Constant;
import com.ruiyun.jvppeteer.common.PrimitiveValue;
import com.ruiyun.jvppeteer.exception.JvppeteerException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


import static com.ruiyun.jvppeteer.common.Constant.Infinity;
import static com.ruiyun.jvppeteer.common.Constant.NaN;
import static com.ruiyun.jvppeteer.common.Constant.Navigate_Infinity;
import static com.ruiyun.jvppeteer.common.Constant.Navigate_Zero;

public class BidiSerializer {
    public static LocalValue serialize(Object arg) {
        if (arg == null) {
            return new LocalValue("undefined", null);
        }

        switch (arg.getClass().getName()) {
            case "java.lang.Byte":
            case "java.lang.Short":
            case "java.lang.Double":
            case "java.lang.Integer":
            case "java.lang.Long":
            case "java.lang.Float":
                return new LocalValue("number", arg);
            case "java.math.BigInteger":
                return new LocalValue("bigint", arg);
            case "java.lang.String":
                return serializeString(arg);
            case "java.lang.Boolean":
                return new LocalValue("boolean", arg);
            case "com.ruiyun.jvppeteer.common.PrimitiveValue":
                return serializePrimitiveValue((PrimitiveValue) arg);
            default:
                return serializeObject(arg);
        }
    }

    private static LocalValue serializePrimitiveValue(PrimitiveValue arg) {
        return new LocalValue(arg.getValue(), null);
    }

    private static LocalValue serializeObject(Object arg) {
        if (arg instanceof List || arg instanceof Set) {
            List<LocalValue> parsedArray = new ArrayList<>();
            for (Object subArg : (Collection<?>) arg) {
                parsedArray.add(serialize(subArg));
            }
            return new LocalValue("array", parsedArray);
        } else if (arg instanceof Map) {
            List<List<Object>> parsedObject = new ArrayList<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) arg).entrySet()) {
                List<Object> arrayList = new ArrayList<>();
                arrayList.add(serialize(entry.getKey()));
                arrayList.add(serialize(entry.getValue()));
                parsedObject.add(arrayList);
            }
            return new LocalValue("object", parsedObject);
        } else if (arg instanceof java.util.regex.Pattern) {
            java.util.regex.Pattern pattern = (java.util.regex.Pattern) arg;
            ObjectNode value = Constant.OBJECTMAPPER.createObjectNode();
            value.put("pattern", pattern.pattern());
            value.put("flags", pattern.flags());
            return new LocalValue("regexp", value);
        } else if (arg instanceof Date) {
            LocalDateTime localDateTime = ((Date) arg).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            String isoString = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return new LocalValue("date", isoString);
        } else {
            Field[] fields = arg.getClass().getDeclaredFields();
            List<List<Object>> parsedObject = new ArrayList<>();
            for (Field field : fields) {
                field.setAccessible(true);
                List<Object> mappingList = new ArrayList<>();
                mappingList.add(serialize(field.getName()));
                try {
                    mappingList.add(serialize(field.get(arg)));
                } catch (IllegalAccessException e) {
                    throw new JvppeteerException(e);
                }
                parsedObject.add(mappingList);
            }
            return new LocalValue("object", parsedObject);

        }
    }

    private static LocalValue serializeString(Object arg) {
        if (Objects.equals(Navigate_Zero, arg)) {
            return new LocalValue("number", Navigate_Zero);
        } else if (Objects.equals(NaN, arg)) {
            return new LocalValue("number", NaN);
        } else if (Objects.equals(Infinity, arg)) {
            return new LocalValue("number", Infinity);
        } else if (Objects.equals(Navigate_Infinity, arg)) {
            return new LocalValue("number", Navigate_Infinity);
        } else {
            return new LocalValue("string", arg);
        }
    }
}
