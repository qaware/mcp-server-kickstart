package com.qaware.mcp;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings({"java:S112", "java:S1193"})
enum Reflection {

    ;


    public static Object newInstance(String className, Object... initArgs) {
        try {
            return Class.forName(className).getDeclaredConstructor().newInstance(initArgs);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static Class<?> getInnerType(Parameter param) { // meeeh!!!
        Class<?> paramType = param.getType();

        if (paramType.isArray()) return paramType.getComponentType();

        Type genericType = param.getParameterizedType();

        if (genericType instanceof ParameterizedType pType) {
            Type[] typeArgs = pType.getActualTypeArguments();
            if (typeArgs.length == 1) return (Class<?>) typeArgs[0];
        }

        throw new UnsupportedOperationException("Cannot determine inner type for: " + param.getType() + " (parameter: " + param.getName() + ")");
    }


    /**
     * Gets all methods from a class and its superclasses/interfaces.
     * Methods are made accessible and returned in deterministic order.
     */
    public static Set<Method> getMethods(Class<?> klass) {
        Set<Method> methods = new LinkedHashSet<>();

        for (Class<?> subClass : getClasses(klass)) {
            Collections.addAll(methods, sort(subClass.getDeclaredMethods()));
        }

        return setAccessibleTrue(methods);
    }


    /**
     * Gets all methods with specific annotation from a class.
     * Returns a map of method to annotation instance.
     */
    public static <T extends Annotation> Map<Method, T> getMethodsWithAnnotations(Class<?> klass, Class<T> annotationClass) {
        return getMethods(klass)
            .stream()
            .filter(method -> method.isAnnotationPresent(annotationClass))
            .collect(Collectors.toMap(
                method -> method,
                method -> method.getAnnotation(annotationClass)
            ));
    }


    public static boolean isArrayType(Class<?> klass) {
        return    klass.isArray()
               || Collection.class.isAssignableFrom(klass)
               || Set       .class.isAssignableFrom(klass)
               || List      .class.isAssignableFrom(klass);
    }


    public static Object invokeMethod(Method method, Object instance, Map<String, Object> parameters) {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        for (int i = 0; i < methodParams.length; i++) {
            Parameter param = methodParams[i];
            McpParam mcpParam = param.getAnnotation(McpParam.class);

            if (mcpParam == null) {
                throw new IllegalArgumentException("Parameter " + param.getName() + " missing @McpParam annotation");
            }

            Object value = parameters.get(mcpParam.name());

            args[i] = convertValue(param.getType(), value);
        }

        try {
            return method.invoke(instance, args);
        } catch (Exception e) {

            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            throw new RuntimeException(e);
        }
    }


    private static Set<Class<?>> getClasses(Class<?> klass) {
        Set<Class<?>> classes = new LinkedHashSet<>();

        for (; klass != null; klass = klass.getSuperclass()) {
            addClassAndInterfaces(classes, klass);
        }

        return classes;
    }


    private static void addClassAndInterfaces(Collection<Class<?>> classes, Class<?> klass) {
        classes.add(klass);

        for (Class<?> interfaceClass : klass.getInterfaces()) {
            addClassAndInterfaces(classes, interfaceClass);
        }
    }


    private static Method[] sort(Method[] methods) {
        Arrays.sort(methods, Comparator.comparing(Method::getName));
        return methods;
    }


    @SuppressWarnings("java:S3011")
    private static <T extends Collection<? extends AccessibleObject>> T setAccessibleTrue(T accessObjects) {
        for (AccessibleObject accessObject : accessObjects) {
            try {
                accessObject.setAccessible(true);
            } catch (Exception e) {
                // ignore, this is best effort
            }
        }
        return accessObjects;
    }


    private static Object convertValue(Class<?> klass, Object value) {
        return value == null || klass.isInstance(value) ? value
                                                        : Json.fromJson(klass, value.toString());
    }

}
