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

    public static <T extends DaoObject> Field[] getFields(Class<T> currentClass) {
        Field[] fields = _cache.get(currentClass);
        if (fields != null) {
            return fields;
        }

        List<Field> list = loadFields(currentClass);
        fields = list.toArray(new Field[list.size()]);
        _cache.put(currentClass, fields);
        return fields;
    }

    public static <T extends DaoObject> Field getKeyField(Class<T> clazz) {
        Field[] fields = getFields(clazz);
        for(Field field: fields) {
            if(field.getAnnotation(SqlKey.class) != null) {
                return field;
            }
        }
        throw new RuntimeException("SqlKey is not specified in " + clazz.getName());
    }

    public static <T extends DaoObject> Object getKeyValue(T dao) {
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

    @Override
    public String toString() {
        List<String> fields = new ArrayList<>();

        for(Field f: getFields(getClass())) {
            try {
                if(f.getType() == String.class) {
                    fields.add(String.format("%s=\"%s\"", f.getName(), f.get(this)));
                } else if(f.getType() == Character.TYPE) {
                    fields.add(String.format("%s='%s'", f.getName(), f.get(this)));
                } else {
                    fields.add(String.format("%s=%s", f.getName(), f.get(this)));
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                return "ERROR: " + ex.toString();
            }
        }

        return String.format("%s(%s)", getClass().getSimpleName(), String.join(", ", fields));
    }

    public HashMap<String, String> toMap() {
        HashMap<String, String> map = new HashMap<>();
        for(Field f: getFields(getClass())) {
            try {
                map.put(f.getName(), String.valueOf(f.get(this)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    public <T extends DaoObject> T fromMap(Map<String, String> map) {
        for(Field f: getFields(getClass())) {
            try {
                if(map.containsKey(f.getName())) {
                    Class type = f.getType();
                    if(type == Integer.TYPE)
                        f.set(this, Integer.parseInt(map.get(f.getName())));
                    else if(type == Long.TYPE)
                        f.set(this, Long.parseLong(map.get(f.getName())));
                    else if(type == Boolean.TYPE)
                        f.set(this, Boolean.parseBoolean(map.get(f.getName())));
                    else if(type == String.class)
                        f.set(this, map.get(f.getName()));
                    else
                        throw new RuntimeException("Unknown Map conversion type: " + type.toString());
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return (T) this;
    }

    public <T extends DaoObject> void copyTo(T dst) {
        for(Field f: getFields(getClass())) {
            String name = f.getName();
            try {
                Field target = dst.getClass().getField(name);
                target.set(dst, f.get(this));
            } catch (NoSuchFieldException | IllegalAccessException ignore) {}
        }
    }
}
