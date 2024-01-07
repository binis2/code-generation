package net.binis.codegen.compiler;

/*-
 * #%L
 * code-generator
 * %%
 * Copyright (C) 2021 - 2023 Binis Belev
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

import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.JavaCompilerObject;
import net.binis.codegen.exception.GenericCodeGenException;
import net.binis.codegen.factory.CodeFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;
import static net.binis.codegen.tools.Tools.nullCheck;
import static net.binis.codegen.tools.Tools.withRes;

@Slf4j
public class CGList<T extends JavaCompilerObject> extends JavaCompilerObject implements Iterable<T> {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.util.List");
    }

    protected Consumer<CGList<T>> onModify;

    protected Class<T> containedClass;
    protected static Method mAppend;

    protected static Method mGet;

    protected static Method mLast;

    public CGList(Object instance, Consumer<CGList<T>> onModify, Class<T> containedClass) {
        super();
        this.instance = instance;
        this.onModify = onModify;
        this.containedClass = containedClass;
    }

    public static <T extends JavaCompilerObject> CGList<T> nil(Class<T> cls) {
        return new CGList<>(invokeStatic("nil", theClass()), null, cls);
    }

    @Override
    protected void init() {
        cls = theClass();
    }

    public CGList<T> append(T value) {
        if (isNull(mAppend)) {
            mAppend = findMethod("append", cls, Object.class);
        }
        instance = invoke(mAppend, instance, value.getInstance());
        if (nonNull(onModify)) {
            onModify.accept(this);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public static <T extends JavaCompilerObject> CGList<T> from(T[] array, Class<T> cls) {
        var method = findMethod("from", theClass(), Object[].class);
        var params = Arrays.stream(array).map(JavaCompilerObject::getInstance).toArray();
        return new CGList(invokeStatic(method, new Object[]{params}), null, cls);
    }

    public static <T extends JavaCompilerObject> CGList<T> from(List<T> list, Class<T> cls) {
        var result = CGList.nil(cls);
        list.forEach(result::append);
        return result;
    }

    public int size() {
        return (int) invoke("size", instance);
    }

    @SuppressWarnings("unchecked")
    public T get(int index) {
        if (isNull(mGet)) {
            mGet = findMethod("get", cls, int.class);
        }
        var inst = invoke(mGet, instance, index);
        if (nonNull(inst)) {
            if (containedClass.equals(JavaCompilerObject.class)) {
                return (T) nullCheck(CodeFactory.create(inst.getClass(), inst), inst);
            } else {
                return CodeFactory.create(containedClass, inst);
            }
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public T last() {
        if (isNull(mLast)) {
            mLast = findMethod("last", cls);
        }
        return withRes(invoke(mLast, instance), inst -> (T) (containedClass.equals(JavaCompilerObject.class) ? CodeFactory.create(inst.getClass(), inst) : CodeFactory.create(containedClass, inst)));
    }

    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {
        return new ProxyIterator((Iterator) invoke("iterator", instance), containedClass);
    }

    protected static class ProxyIterator implements Iterator {

        protected Iterator iterator;
        protected Constructor constructor;
        protected Function<Object, Object> func;

        public ProxyIterator(Iterator iterator, Class cls) {
            this.iterator = iterator;
            if (JavaCompilerObject.class.equals(cls)) {
                func = inst ->
                        nullCheck(CodeFactory.create(inst.getClass(), inst), inst);
            } else {
                constructor = findConstructor(cls, Object.class);
                func = inst -> {
                    try {
                        return constructor.newInstance(inst);
                    } catch (Exception e) {
                        throw new GenericCodeGenException(e);
                    }
                };
            }
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Object next() {
            return func.apply(iterator.next());
        }
    }
}
