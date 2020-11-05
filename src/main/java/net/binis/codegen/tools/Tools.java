package net.binis.codegen.tools;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

public class Tools {

    private Tools() {
        //Do nothing
    }

    public static <T, R> T nullCheck(R object, Function<R, T> func) {
        return nullCheck(object, func, null);
    }

    public static <T> T nullCheck(T object, T defaultObject) {
        return nonNull(object) ? object : defaultObject;
    }

    public static <T, R, Q> T nullCheck(R object, Class<Q> iface, Function<Q, T> func) {
        return nonNull(object) ? func.apply(iface.cast(object)) : null;
    }

    public static <T, R> T nullCheck(R object, Function<R, T> func, T defaultObject) {
        return nonNull(object) ? func.apply(object) : defaultObject;
    }

    public static <R> void notNull(R object, Consumer<R> consumer) {
        if (object != null) {
            consumer.accept(object);
        }
    }

    public static <R> void with(R object, Consumer<R> consumer) {
        consumer.accept(object);
    }


    public static <R> R ifNull(R object, Supplier<R> supplier) {
        if (object == null) {
            return supplier.get();
        }
        return object;
    }

    public static void ifNull(Object object, Runnable runnable) {
        if (object == null) {
            runnable.run();
        }
    }

    public static <R> void conditional(R object, Predicate<R> condition, Consumer<R> consumer) {
        if (object != null && condition.test(object)) {
            consumer.accept(object);
        }
    }

    public static <T> T condition(boolean condition, Supplier<T> supplier) {
        return condition ? supplier.get() : null;
    }

    public static <T> T condition(boolean condition, T constant) {
        return condition ? constant : null;
    }

    public static void condition(boolean condition, Runnable task) {
        if (condition) {
            task.run();
        }
    }

}
