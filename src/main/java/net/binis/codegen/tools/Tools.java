package net.binis.codegen.tools;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 Binis Belev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import lombok.val;

import java.util.Optional;
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
        if (nonNull(object)) {
            consumer.accept(object);
        }
    }

    public static <R> void with(R object, Consumer<R> consumer) {
        if (nonNull(object)) {
            consumer.accept(object);
        }
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

    public static <T, R> T emptyCheck(Optional<R> optional, Function<R, T> func, T defaultObject) {
        return optional.isPresent() ? func.apply(optional.get()) : defaultObject;
    }

    public static <T, R, Q> T condition(boolean condition, R object, Class<Q> iface, Function<Q, T> func) {
        return condition && nonNull(object) ? func.apply(iface.cast(object)) : null;
    }

    public static <T> T safeCall(Supplier<T> func, Function<Exception, T> onException) {
        try {
            return func.get();
        } catch (Exception e) {
            return onException.apply(e);
        }
    }

    public static <T, R> R withRes(T object, Function<T, R> wither) {
        if (nonNull(object)) {
            return wither.apply(object);
        }
        return null;
    }

    @SafeVarargs
    public static <T> boolean in(T object, T... list) {
        for (var o : list) {
            if (o.equals(object)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allNotNull(Object... values) {
        if (values == null) {
            return false;
        } else {
            for (var value : values) {
                if (value == null) {
                    return false;
                }
            }
            return true;
        }
    }

    public static boolean anyNull(Object... values) {
        return !allNotNull(values);
    }

    public static long measure(Runnable run) {
        val start = System.currentTimeMillis();
        run.run();
        return System.currentTimeMillis() - start;
    }


}

