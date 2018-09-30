package org.megastage.vase;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class DaoObject {
    private static transient HashMap<Class, Field[]> _cache = new HashMap<>();

    private static boolean copyableField(Field f) {
        int modifiers = f.getModifiers();
        return !(Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers));
    }

    public static <T> Field[] getFields(Class<?> currentClass) {
        Field[] fields = _cache.get(currentClass);
        if (fields != null) {
            return fields;
        }

        List<Field> list = loadFields(currentClass);
        fields = list.toArray(new Field[list.size()]);
        _cache.put(currentClass, fields);
        return fields;
    }

    public static <T> Field getKeyField(Class<T> clazz) {
        Field[] fields = getFields(clazz);
        for(Field field: fields) {
            if(field.getAnnotation(SqlKey.class) != null) {
                return field;
            }
        }
        throw new RuntimeException("SqlKey is not specified in " + clazz.getName());
    }

    public static <T> Object getKeyValue(T dao) {
        Field[] fields = getFields(dao.getClass());
        try {
            for(Field field: fields) {
                if(field.getAnnotation(SqlKey.class) != null) {
                        return field.get(dao);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        throw new RuntimeException("SqlKey is not specified in " + dao.getClass().getName());
    }

    private static List<Field> loadFields(Class<?> klass) {
        Field[] declaredFields = klass.getDeclaredFields();
        List<Field> result = new ArrayList<>(declaredFields.length);
        for (Field f : declaredFields) {
            if (copyableField(f)) {
                f.setAccessible(true);
                result.add(f);
            }
        }
        klass = klass.getSuperclass();
        if (klass != null) {
            result.addAll(loadFields(klass));
        }
        return result;
    }

    public static String toString(Object obj) {
        List<String> fields = new ArrayList<>();

        for(Field f: getFields(obj.getClass())) {
            try {
                if(f.getType() == String.class) {
                    fields.add(String.format("%s=\"%s\"", f.getName(), f.get(obj)));
                } else if(f.getType() == Character.TYPE) {
                    fields.add(String.format("%s='%s'", f.getName(), f.get(obj)));
                } else {
                    fields.add(String.format("%s=%s", f.getName(), f.get(obj)));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return "ERROR: " + ex.toString();
            }
        }

        return String.format("%s(%s)", obj.getClass().getSimpleName(), String.join(", ", fields));
    }
}
