package net.binis.codegen.compiler;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.binis.codegen.compiler.base.BaseJavaCompilerObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.Consumer;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static net.binis.codegen.tools.Reflection.*;

@Slf4j
public class CGList<T extends BaseJavaCompilerObject> extends BaseJavaCompilerObject {

    public static Class theClass() {
        return loadClass("com.sun.tools.javac.util.List");
    }

    protected final Consumer<CGList<T>> onModify;
    protected Method mAppend;

    public CGList(Object instance, Consumer<CGList<T>> onModify) {
        super();
        this.instance = instance;
        this.onModify = onModify;
    }

    public static <T extends BaseJavaCompilerObject> CGList<T> nil() {
        return new CGList<>(invokeStatic("nil", theClass()), null);
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
    public Iterator<T> iterator(Class<T> cls) {
        return new ProxyIterator((Iterator) invoke("iterator", instance), cls);
    }

    protected static class ProxyIterator implements Iterator {

        protected Iterator iterator;
        protected Constructor constructor;

        public ProxyIterator(Iterator iterator, Class cls) {
            this.iterator = iterator;
            constructor = findConstructor(cls, Object.class);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @SneakyThrows
        @Override
        public Object next() {
            return constructor.newInstance(iterator.next());
        }
    }
}
