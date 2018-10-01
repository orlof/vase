package org.megastage.vase;

import com.esotericsoftware.minlog.Log;
import org.reflections.Reflections;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.*;
import java.util.stream.Collectors;

public class Assembler {
    private Map<Class, Object> components;

    public static void main( String[] args ) {
        Assembler me = new Assembler();
        me.setup("org.megastage.vase.example");
        me.shutdown();
    }

    public void setup(String rootPackage) {
        instantiate(rootPackage);
        inject();
        initialize();
    }

    public void instantiate(String rootPackage) {
        Reflections reflections = new Reflections(rootPackage);

        components = new HashMap<>();

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(VaseComponent.class);
        for(Class clazz: classes) {
            try {
                Log.debug(String.format("Instantiate: %s", clazz.getName()));
                components.put(clazz, clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public void inject() {
        for(Object comp: components.values()) {
            for(Field field : comp.getClass().getDeclaredFields()) {
                if(field.isAnnotationPresent(VaseInject.class)) {
                    try {
                        boolean access = field.isAccessible();
                        if(!access) {
                            field.setAccessible(true);
                        }

                        Log.debug(String.format("Injecting dependency %s.%s = %s",
                                comp.getClass().getSimpleName(),
                                field.getName(),
                                field.getType().getSimpleName()));

                        if(!components.containsKey(field.getType())) {
                            throw new RuntimeException("Unknown VaseComponent");
                        }

                        field.set(comp, components.get(field.getType()));

                        if(!access) {
                            field.setAccessible(false);
                        }
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void initialize() {
        execute("initialize", false);
    }

    public void shutdown() {
        execute("shutdown", true);
    }

    public void execute(String method, boolean reverse) {
        final int sign = reverse ? -1: 1;

        for(Object comp: components.values().stream()
                .sorted(Comparator.comparingInt(c -> sign * c.getClass().getAnnotation(VaseComponent.class).value()))
                .collect(Collectors.toList())) {
            try {
                Method m = comp.getClass().getMethod(method);
                try {
                    m.invoke(comp);
                } catch(Exception e) {
                    Log.error(String.format("%s in %s.%s()", e.getMessage(), comp.getClass().getSimpleName(), method));
                }
            } catch (NoSuchMethodException ignore) {}
        }
    }

}
