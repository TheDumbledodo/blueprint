package com.github.thedumbledodo.blueprint.config.mapper;

import com.github.thedumbledodo.blueprint.config.ConfigException;
import com.github.thedumbledodo.blueprint.config.ConfigProperties;
import com.github.thedumbledodo.blueprint.config.annotation.Comment;
import com.github.thedumbledodo.blueprint.config.model.ConfigSection;
import com.github.thedumbledodo.blueprint.config.serializer.Serializer;
import com.github.thedumbledodo.blueprint.config.serializer.Serializers;
import com.github.thedumbledodo.blueprint.reflect.Primitives;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigMapper {

    private static final Map<Class<?>, List<Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    private final ConfigProperties properties;
    private final Map<Class<?>, Serializer<?, ?>> serializers = new LinkedHashMap<>();

    public ConfigMapper(ConfigProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");

        serializers.putAll(properties.getSerializers());
        Serializers.defaults().forEach(serializers::putIfAbsent);
    }

    public ConfigSection serialize(Object instance) {
        Objects.requireNonNull(instance, "instance");

        final ConfigSection section = serializeObject(instance, instance.getClass(), "");
        final Comment header = instance.getClass().getAnnotation(Comment.class);

        if (header != null) {
            section.setHeader(List.of(header.value()));
        }
        return section;
    }

    public void apply(Map<String, ?> values, Object target) {
        Objects.requireNonNull(target, "target");

        if (values == null) {
            return;
        }
        applyObject(values, target, "");
    }

    private ConfigSection serializeObject(Object instance, Class<?> type, String path) {
        final ConfigSection section = new ConfigSection();

        if (type.isRecord()) {
            for (RecordComponent component : type.getRecordComponents()) {
                final String key = properties.getNameFormatter().format(component.getName());
                final Object value = invoke(component.getAccessor(), instance, join(path, key));

                put(section, key, serializeValue(value, component.getGenericType(), join(path, key)), getComments(type, component.getName()));
            }
            return section;
        }

        rejectPlatformType(type, path);

        for (Field field : getFields(type)) {
            final String key = properties.getNameFormatter().format(field.getName());
            final Object value = get(field, instance, join(path, key));

            put(section, key, serializeValue(value, field.getGenericType(), join(path, key)), getComments(field));
        }
        return section;
    }

    private void put(ConfigSection section, String key, Object value, List<String> comments) {
        if (value == null && !properties.isOutputNulls()) {
            return;
        }
        section.set(key, value, comments);
    }

    private Object serializeValue(Object value, Type type, String path) {
        if (value == null) {
            return null;
        }

        final Serializer<Object, Object> serializer = findSerializer(Primitives.wrap(erase(type)), value.getClass());

        if (serializer != null) {
            return ConfigSection.fromValue(serializer.serialize(value));
        }

        if (value instanceof BigInteger || value instanceof BigDecimal || value instanceof Character) {
            return value.toString();
        }

        if (value instanceof String || value instanceof Boolean || value instanceof Number) {
            return value;
        }

        if (value instanceof Enum<?> constant) {
            return constant.name();
        }

        if (value instanceof Collection<?> || value.getClass().isArray()) {
            final Type elementType = value instanceof Collection<?> ? typeArgument(type, 0) : componentType(type, value.getClass());
            final List<?> elements = asList(value, path);
            final List<Object> list = new ArrayList<>(elements.size());

            for (int i = 0; i < elements.size(); i++) {
                list.add(serializeValue(elements.get(i), elementType, path + "[" + i + "]"));
            }
            return list;
        }

        if (value instanceof Map<?, ?> map) {
            final Type valueType = typeArgument(type, 1);
            final ConfigSection section = new ConfigSection();

            for (Map.Entry<?, ?> entry : map.entrySet()) {
                final String key = keyToString(entry.getKey(), path);
                final Object serialized = serializeValue(entry.getValue(), valueType, join(path, key));

                put(section, key, serialized, List.of());
            }
            return section;
        }
        return serializeObject(value, value.getClass(), path);
    }

    private void applyObject(Map<String, ?> values, Object target, String path) {
        for (Field field : getFields(target.getClass())) {
            final String key = properties.getNameFormatter().format(field.getName());

            if (!values.containsKey(key) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }

            final String fieldPath = join(path, key);
            final Object current = get(field, target, fieldPath);
            final Object value = deserializeValue(values.get(key), field.getGenericType(), current, fieldPath);

            if (value == null && (field.getType().isPrimitive() || !properties.isInputNulls())) {
                continue;
            }

            try {
                field.set(target, value);

            } catch (IllegalAccessException exception) {
                throw new ConfigException(fieldPath + ": cannot write field " + field.getName(), exception);
            }
        }
    }

    private Object deserializeValue(Object raw, Type type, Object current, String path) {
        if (raw == null) {
            return null;
        }

        final Class<?> clazz = Primitives.wrap(erase(type));
        final Serializer<Object, Object> serializer = findSerializer(clazz);

        if (serializer != null) {
            try {
                return serializer.deserialize(ConfigSection.toPlain(raw));

            } catch (ConfigException exception) {
                throw exception;

            } catch (RuntimeException exception) {
                throw error(path, "could not read " + clazz.getSimpleName() + " from " + describe(raw) + " (" + exception.getMessage() + ")", exception);
            }
        }

        if (clazz == Object.class) {
            return ConfigSection.toPlain(raw);
        }

        if (clazz == String.class) {
            if (raw instanceof Map<?, ?> || raw instanceof ConfigSection || raw instanceof Collection<?>) {
                throw error(path, "expected text but got " + describe(raw));
            }
            return String.valueOf(raw);
        }

        if (clazz == Boolean.class) {
            return toBoolean(raw, path);
        }

        if (clazz == Character.class) {
            final String text = String.valueOf(raw);

            if (text.length() != 1) {
                throw error(path, "expected a single character but got \"" + text + "\"");
            }
            return text.charAt(0);
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return toNumber(raw, clazz, path);
        }

        if (clazz.isEnum()) {
            return toEnum(raw, clazz, path);
        }

        if (Collection.class.isAssignableFrom(clazz)) {
            return toCollection(raw, type, clazz, path);
        }

        if (clazz.isArray()) {
            return toArray(raw, type, clazz, path);
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return toMap(raw, type, clazz, current, path);
        }

        if (clazz.isRecord()) {
            return toRecord(asMap(raw, path), clazz, current, path);
        }

        rejectPlatformType(clazz, path);

        if (isAbstract(clazz)) {
            throw error(path, "no serializer registered for " + clazz.getName());
        }

        final Object instance = clazz.isInstance(current) ? current : newInstance(clazz, path);

        applyObject(asMap(raw, path), instance, path);
        return instance;
    }

    private Boolean toBoolean(Object raw, String path) {
        if (raw instanceof Boolean bool) {
            return bool;
        }

        final String text = String.valueOf(raw).trim();

        if (text.equalsIgnoreCase("true")) {
            return true;
        }

        if (text.equalsIgnoreCase("false")) {
            return false;
        }
        throw error(path, "expected true or false but got " + describe(raw));
    }

    private Object toNumber(Object raw, Class<?> clazz, String path) {
        final BigDecimal decimal = toDecimal(raw, path);

        if (decimal == null) {
            final double value = ((Number) raw).doubleValue();

            if (clazz == Double.class) {
                return value;
            }

            if (clazz == Float.class) {
                return (float) value;
            }
            throw error(path, "expected a number but got " + raw);
        }

        if (clazz == Double.class) {
            return decimal.doubleValue();
        }

        if (clazz == Float.class) {
            return decimal.floatValue();
        }

        if (clazz == BigDecimal.class) {
            return decimal;
        }

        if (decimal.stripTrailingZeros().scale() > 0) {
            throw error(path, "expected a whole number but got " + decimal.toPlainString());
        }

        final BigInteger integer = decimal.toBigIntegerExact();

        if (clazz == BigInteger.class) {
            return integer;
        }

        if (clazz == Long.class) {
            return checkRange(integer, Long.MIN_VALUE, Long.MAX_VALUE, path).longValue();
        }

        if (clazz == Integer.class) {
            return checkRange(integer, Integer.MIN_VALUE, Integer.MAX_VALUE, path).intValue();
        }

        if (clazz == Short.class) {
            return checkRange(integer, Short.MIN_VALUE, Short.MAX_VALUE, path).shortValue();
        }

        if (clazz == Byte.class) {
            return checkRange(integer, Byte.MIN_VALUE, Byte.MAX_VALUE, path).byteValue();
        }
        throw error(path, "unsupported number type " + clazz.getName());
    }

    private BigDecimal toDecimal(Object raw, String path) {
        if (raw instanceof BigDecimal decimal) {
            return decimal;
        }

        if (raw instanceof Double || raw instanceof Float) {
            final double value = ((Number) raw).doubleValue();

            if (Double.isNaN(value) || Double.isInfinite(value)) {
                return null;
            }
            return BigDecimal.valueOf(value);
        }

        if (raw instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        if (raw instanceof String text) {
            try {
                return new BigDecimal(text.trim());

            } catch (NumberFormatException exception) {
                throw error(path, "expected a number but got \"" + text + "\"");
            }
        }
        throw error(path, "expected a number but got " + describe(raw));
    }

    private BigInteger checkRange(BigInteger value, long min, long max, String path) {
        if (value.compareTo(BigInteger.valueOf(min)) < 0 || value.compareTo(BigInteger.valueOf(max)) > 0) {
            throw error(path, value + " is out of range (" + min + " to " + max + ")");
        }
        return value;
    }

    private Object toEnum(Object raw, Class<?> clazz, String path) {
        final String name = String.valueOf(raw).trim();
        final Object[] constants = clazz.getEnumConstants();

        for (Object constant : constants) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(name)) {
                return constant;
            }
        }
        throw error(path, "expected one of " + Arrays.toString(constants) + " but got \"" + name + "\"");
    }

    private Object toCollection(Object raw, Type type, Class<?> clazz, String path) {
        final List<?> list = asList(raw, path);
        final Type elementType = typeArgument(type, 0);
        final Collection<Object> collection = newCollection(clazz, path);

        for (int i = 0; i < list.size(); i++) {
            collection.add(deserializeValue(list.get(i), elementType, null, path + "[" + i + "]"));
        }
        return collection;
    }

    private Object toArray(Object raw, Type type, Class<?> clazz, String path) {
        final List<?> list = asList(raw, path);
        final Type componentType = componentType(type, clazz);
        final Object array = Array.newInstance(erase(componentType), list.size());

        for (int i = 0; i < list.size(); i++) {
            final Object value = deserializeValue(list.get(i), componentType, null, path + "[" + i + "]");

            if (value == null && erase(componentType).isPrimitive()) {
                continue;
            }
            Array.set(array, i, value);
        }
        return array;
    }

    private Object toMap(Object raw, Type type, Class<?> clazz, Object current, String path) {
        final Map<String, ?> values = asMap(raw, path);
        final Type keyType = typeArgument(type, 0);
        final Type valueType = typeArgument(type, 1);
        final Map<Object, Object> map = newMap(clazz, path);
        final Map<?, ?> currentMap = current instanceof Map<?, ?> existing ? existing : Map.of();

        for (Map.Entry<String, ?> entry : values.entrySet()) {
            final Object key = keyFromString(entry.getKey(), keyType, join(path, entry.getKey()));
            final Object value = deserializeValue(entry.getValue(), valueType, currentMap.get(key), join(path, entry.getKey()));

            map.put(key, value);
        }
        return map;
    }

    private Object toRecord(Map<String, ?> values, Class<?> clazz, Object current, String path) {
        final RecordComponent[] components = clazz.getRecordComponents();
        final Object[] arguments = new Object[components.length];
        final Class<?>[] types = new Class<?>[components.length];

        for (int i = 0; i < components.length; i++) {
            final RecordComponent component = components[i];
            final String key = properties.getNameFormatter().format(component.getName());
            final Object currentValue = current != null ? invoke(component.getAccessor(), current, join(path, key)) : null;

            Object value = currentValue;

            if (values.containsKey(key)) {
                value = deserializeValue(values.get(key), component.getGenericType(), currentValue, join(path, key));
            }

            if (value == null && component.getType().isPrimitive()) {
                value = Array.get(Array.newInstance(component.getType(), 1), 0);
            }

            types[i] = component.getType();
            arguments[i] = value;
        }

        try {
            final Constructor<?> constructor = clazz.getDeclaredConstructor(types);

            constructor.setAccessible(true);
            return constructor.newInstance(arguments);

        } catch (InvocationTargetException exception) {
            throw error(path, "could not create " + clazz.getSimpleName() + ": " + exception.getCause().getMessage(), exception.getCause());

        } catch (ReflectiveOperationException exception) {
            throw error(path, "could not create " + clazz.getSimpleName(), exception);
        }
    }

    private Object keyFromString(String key, Type keyType, String path) {
        final Class<?> clazz = Primitives.wrap(erase(keyType));

        if (clazz == String.class || clazz == Object.class) {
            return key;
        }

        if (Number.class.isAssignableFrom(clazz)) {
            return toNumber(key, clazz, path);
        }

        if (clazz.isEnum()) {
            return toEnum(key, clazz, path);
        }

        if (clazz == Boolean.class) {
            return toBoolean(key, path);
        }

        if (clazz == UUID.class) {
            try {
                return UUID.fromString(key);

            } catch (IllegalArgumentException exception) {
                throw error(path, "expected a UUID key but got \"" + key + "\"");
            }
        }
        throw error(path, "map keys of type " + clazz.getName() + " are not supported");
    }

    private String keyToString(Object key, String path) {
        if (key instanceof String text) {
            return text;
        }

        if (key instanceof Enum<?> constant) {
            return constant.name();
        }

        if (key instanceof Number || key instanceof Boolean || key instanceof UUID || key instanceof Character) {
            return String.valueOf(key);
        }
        throw error(path, "map keys of type " + (key == null ? "null" : key.getClass().getName()) + " are not supported");
    }

    private Map<String, ?> asMap(Object raw, String path) {
        if (raw instanceof ConfigSection || raw instanceof Map<?, ?>) {
            return (Map<String, ?>) ConfigSection.toPlain(raw);
        }
        throw error(path, "expected a section but got " + describe(raw));
    }

    private List<?> asList(Object raw, String path) {
        if (raw instanceof List<?> list) {
            return list;
        }

        if (raw instanceof Collection<?> collection) {
            return new ArrayList<>(collection);
        }

        if (raw != null && raw.getClass().isArray()) {
            final List<Object> list = new ArrayList<>(Array.getLength(raw));

            for (int i = 0; i < Array.getLength(raw); i++) {
                list.add(Array.get(raw, i));
            }
            return list;
        }
        throw error(path, "expected a list but got " + describe(raw));
    }

    private Collection<Object> newCollection(Class<?> clazz, String path) {
        if (isAbstract(clazz)) {
            if (SortedSet.class.isAssignableFrom(clazz)) {
                return new TreeSet<>();
            }

            if (Set.class.isAssignableFrom(clazz)) {
                return new LinkedHashSet<>();
            }

            if (Deque.class.isAssignableFrom(clazz) || Queue.class.isAssignableFrom(clazz)) {
                return new ArrayDeque<>();
            }
            return new ArrayList<>();
        }
        return (Collection<Object>) newInstance(clazz, path);
    }

    private Map<Object, Object> newMap(Class<?> clazz, String path) {
        if (isAbstract(clazz)) {
            if (SortedMap.class.isAssignableFrom(clazz)) {
                return new TreeMap<>();
            }
            return new LinkedHashMap<>();
        }
        return (Map<Object, Object>) newInstance(clazz, path);
    }

    private Object newInstance(Class<?> clazz, String path) {
        try {
            final Constructor<?> constructor = clazz.getDeclaredConstructor();

            constructor.setAccessible(true);
            return constructor.newInstance();

        } catch (NoSuchMethodException exception) {
            throw error(path, clazz.getName() + " needs a no-arg constructor or a registered serializer", exception);

        } catch (InvocationTargetException exception) {
            throw error(path, "could not create " + clazz.getName() + ": " + exception.getCause().getMessage(), exception.getCause());

        } catch (ReflectiveOperationException exception) {
            throw error(path, "could not create " + clazz.getName(), exception);
        }
    }

    private Serializer<Object, Object> findSerializer(Class<?>... types) {
        for (Class<?> type : types) {
            final Serializer<?, ?> serializer = serializers.get(type);

            if (serializer != null) {
                return (Serializer<Object, Object>) serializer;
            }
        }

        for (Class<?> type : types) {
            for (Map.Entry<Class<?>, Serializer<?, ?>> entry : serializers.entrySet()) {
                if (isAbstract(entry.getKey()) && entry.getKey().isAssignableFrom(type)) {
                    return (Serializer<Object, Object>) entry.getValue();
                }
            }
        }
        return null;
    }

    private void rejectPlatformType(Class<?> type, String path) {
        final String name = type.getName();

        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.") || name.startsWith("sun.")) {
            throw error(path, "no serializer registered for " + name);
        }
    }

    private static List<Field> getFields(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, key -> {
            final List<Class<?>> hierarchy = new ArrayList<>();

            for (Class<?> current = key; current != null && current != Object.class; current = current.getSuperclass()) {
                hierarchy.addFirst(current);
            }

            final List<Field> fields = new ArrayList<>();

            for (Class<?> clazz : hierarchy) {
                for (Field field : clazz.getDeclaredFields()) {
                    final int modifiers = field.getModifiers();

                    if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) {
                        continue;
                    }

                    field.setAccessible(true);
                    fields.add(field);
                }
            }
            return List.copyOf(fields);
        });
    }

    private static List<String> getComments(Field field) {
        final Comment comment = field.getAnnotation(Comment.class);

        return comment == null ? List.of() : List.of(comment.value());
    }

    private static List<String> getComments(Class<?> type, String name) {
        try {
            return getComments(type.getDeclaredField(name));

        } catch (NoSuchFieldException exception) {
            return List.of();
        }
    }

    private static Object get(Field field, Object instance, String path) {
        try {
            return field.get(instance);

        } catch (IllegalAccessException exception) {
            throw new ConfigException(path + ": cannot read field " + field.getName(), exception);
        }
    }

    private static Object invoke(Method method, Object instance, String path) {
        try {
            method.setAccessible(true);
            return method.invoke(instance);

        } catch (ReflectiveOperationException exception) {
            throw new ConfigException(path + ": cannot read " + method.getName(), exception);
        }
    }

    private static Type typeArgument(Type type, int index) {
        if (type instanceof ParameterizedType parameterized) {
            final Type[] arguments = parameterized.getActualTypeArguments();

            if (index < arguments.length) {
                return arguments[index];
            }
        }
        return Object.class;
    }

    private static Type componentType(Type type, Class<?> arrayClass) {
        if (type instanceof GenericArrayType genericArray) {
            return genericArray.getGenericComponentType();
        }
        return arrayClass.getComponentType();
    }

    private static Class<?> erase(Type type) {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }

        if (type instanceof ParameterizedType parameterized) {
            return (Class<?>) parameterized.getRawType();
        }

        if (type instanceof GenericArrayType genericArray) {
            return Array.newInstance(erase(genericArray.getGenericComponentType()), 0).getClass();
        }

        if (type instanceof WildcardType wildcard) {
            return erase(wildcard.getUpperBounds()[0]);
        }

        if (type instanceof TypeVariable<?> variable) {
            return erase(variable.getBounds()[0]);
        }
        return Object.class;
    }

    private static boolean isAbstract(Class<?> type) {
        return type.isInterface() || Modifier.isAbstract(type.getModifiers());
    }

    private static String join(String path, String key) {
        return path.isEmpty() ? key : path + "." + key;
    }

    private static String describe(Object value) {
        if (value instanceof Map<?, ?> || value instanceof ConfigSection) {
            return "a section";
        }

        if (value instanceof Collection<?>) {
            return "a list";
        }

        if (value instanceof String text) {
            return "\"" + text + "\"";
        }
        return String.valueOf(value);
    }

    private static ConfigException error(String path, String message) {
        return new ConfigException(path + ": " + message);
    }

    private static ConfigException error(String path, String message, Throwable cause) {
        return new ConfigException(path + ": " + message, cause);
    }
}
