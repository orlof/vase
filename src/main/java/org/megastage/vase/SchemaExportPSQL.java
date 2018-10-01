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

        Set<String> allTables = new HashSet<>();

        Map<String, String> create = new HashMap<>();
        Map<String, String> drop = new HashMap<>();

        for(Class clazz: classes) {
            String tableName = ((SqlTableName) clazz.getAnnotation(SqlTableName.class)).value();
            allTables.add(tableName);

            if(clazz.isEnum()) {
                String[] values = getNames(clazz);
                create.put(tableName, String.format("CREATE TYPE %s AS ENUM (%s);\n", tableName, String.join(", ", values)));
                drop.put(tableName, String.format("DROP TYPE IF EXISTS %s;", tableName));
            } else {
                List<String> cols = Arrays.stream(DaoObject.getFields(clazz))
                        .map(f -> exportCol(f, tableName))
                        .collect(Collectors.toList());

                create.put(tableName, String.format("CREATE TABLE %s (\n%s\n);\n", tableName, String.join(",\n", cols)));
                drop.put(tableName, String.format("DROP TABLE IF EXISTS %s;", tableName));
            }
        }

        List<String> order = new ArrayList<>();

        Set<String> free = getFree(allTables);
        while(!free.isEmpty()) {
            allTables.removeAll(free);
            order.addAll(free.stream().sorted().collect(Collectors.toList()));
            Iterator<Link> it = links.iterator();
            while(it.hasNext()) {
                if(free.contains(it.next().to)) {
                    it.remove();
                }
            }
            free = getFree(allTables);
        }

        if(allTables.isEmpty()) {
            List<String> output = new ArrayList<>();
            for (String table : order) {
                output.add(drop.get(table));
            }
            Collections.reverse(output);
            output.add("");

            for (String table : order) {
                output.add(create.get(table));
            }

            return String.join("\n", output);
        }

        throw new RuntimeException("CircularReferenceException");
    }

    private Set<String> getFree(Set<String> all) {
        Set<String> free = new HashSet<>();
        free.addAll(all);

        Set<String> notFree = new HashSet<>();
        notFree.addAll(links.stream()
                .map(link -> link.from)
                .collect(Collectors.toSet()));

        free.removeAll(notFree);
        return free;
    }

    public String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }

    private String exportCol(Field f, String table) {
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
            String target = ((SqlTableName) references.getAnnotation(SqlTableName.class)).value();
            opts.add(String.format("REFERENCES %s", target));
            links.add(new Link(table, target));
        }

        return String.format("    %s %s", f.getName(), String.join(" ", opts));
    }

    private List<Link> links = new ArrayList<>();

    private static class Link {
        String from;
        String to;

        public Link(String from, String to) {
            this.from = from;
            this.to = to;
        }
    }
}
