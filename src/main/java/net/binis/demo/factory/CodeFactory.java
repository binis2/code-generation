package net.binis.demo.factory;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CodeFactory {

    private static Map<Class<?>, RegistryEntry> registry = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> cls) {
        try {
            var entry = registry.get(cls);
            if (entry != null) {
                var creator = entry.getCreator();
                if (creator == null) {
                    creator = tryGenerateCreator(entry.implClass);
                    entry.setCreator(creator);
                }

                if (creator != null) {
                    return (T) creator.get();
                } else {
                    throw new RuntimeException("Can't instantiate " + cls.getName());
                }
            } else {
                throw new RuntimeException("There is no registry for " + cls.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <M, T, P> M modify(P parent, T value) {
        try {
            var entry = registry.get(value.getClass());
            if (entry != null) {
                var modifier = entry.getModifier();
                if (modifier == null) {
                    modifier = tryGenerateModifier(entry.modifierClass);
                    entry.setModifier(modifier);
                }

                if (modifier != null) {
                    return (M) modifier.apply(parent, value);
                } else {
                    throw new RuntimeException("Can't instantiate " + value.getClass().getName());
                }
            } else {
                throw new RuntimeException("There is no registry for " + value.getClass().getName());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Supplier<Object> tryGenerateCreator(Class<?> implClass) {
        return () -> {
            try {
                return implClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static BiFunction<Object, Object, Object> tryGenerateModifier(Class<?> modifierClass) {
        try {
            var constructor = modifierClass.getDeclaredConstructor(Object.class, Object.class);

            return (parent, entity) -> {
                try {
                    return constructor.newInstance(parent, entity);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


    public static void registerEmbeddableType(Class<?> intf, Class<?> impl, Class<?> modifier) {
        registry.put(intf, RegistryEntry.builder().implClass(impl).modifierClass(modifier).build());
    }

    @Data
    @Builder
    private static class RegistryEntry {
        private Class<?> implClass;
        private Class<?> modifierClass;

        private Supplier<Object> creator;
        private BiFunction<Object, Object, Object> modifier;
    }
}
