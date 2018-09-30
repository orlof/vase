package org.megastage.vase;

import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class SchemaExportPSQL {
    public static void main(String[] args) throws Exception {
        SchemaExportPSQL me = new SchemaExportPSQL();
        String schema = me.export("org.megastage.vase");
        System.out.println(schema);
    }

    public String export(String rootPackage) {
        Reflections reflections = new Reflections(rootPackage);
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(SqlTableName.class);

        List<String> dropTypes = new ArrayList<>();
        List<String> dropTables = new ArrayList<>();
        List<String> types = new ArrayList<>();
        List<String> tables = new ArrayList<>();
        for(Class clazz: classes) {
            String tableName = ((SqlTableName) clazz.getAnnotation(SqlTableName.class)).value();
            if(clazz.isEnum()) {
                String[] values = getNames(clazz);
                types.add(String.format("CREATE TYPE %s AS ENUM (%s);\n", tableName, String.join(", ", values)));
                dropTypes.add(String.format("DROP TYPE IF EXISTS %s", tableName));
            } else {
                List<String> cols = Arrays.stream(DaoObject.getFields(clazz))
                        .map(this::exportCol)
                        .collect(Collectors.toList());

                tables.add(String.format("CREATE TABLE %s (\n%s\n);\n", tableName, String.join(",\n", cols)));
                dropTables.add(String.format("DROP TABLE IF EXISTS %s", tableName));
            }
        }

        List<String> all = new ArrayList<>();
        all.addAll(dropTables);
        all.add("");
        all.addAll(dropTypes);
        all.add("");
        all.addAll(types);
        all.addAll(tables);

        return String.join("\n", all);
    }

    public String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    private String exportCol(Field f) {
        List<String> opts = new ArrayList<>();
        if(f.getAnnotation(SqlSerial.class) != null) {
            opts.add("SERIAL");
        } else if(f.getType().isEnum()) {
            opts.add(f.getType().getAnnotation(SqlTableName.class).value());
        } else if(f.getType() == Boolean.TYPE) {
            opts.add("BOOLEAN");
        } else if(f.getType() == Integer.TYPE) {
            opts.add("INTEGER");
        } else if(f.getType() == String.class) {
            opts.add("VARCHAR(255)");
        } else if(f.getType() == Timestamp.class) {
            opts.add("TIMESTAMP");
        }

        if(f.getAnnotation(SqlNotNull.class) != null) {
            opts.add("NOT NULL");
        }
        if(f.getAnnotation(SqlUnique.class) != null) {
            opts.add("UNIQUE");
        }
        if(f.getAnnotation(SqlReferences.class) != null) {
            if(f.getType() != Integer.TYPE) {
                throw new RuntimeException(String.format("Referencing field type must by int: %s.%s", f.getDeclaringClass(), f.getName()));
            }
            Class references = f.getAnnotation(SqlReferences.class).value();
            opts.add(String.format("REFERENCES %s", ((SqlTableName) references.getAnnotation(SqlTableName.class)).value()));
        }

        return String.format("    %s %s", f.getName(), String.join(" ", opts));
    }
}
