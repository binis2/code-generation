package net.binis.demo.collection;

import net.binis.demo.factory.CodeFactory;

import java.util.Set;

public class EmbeddedCodeSetImpl<M, T, R> implements EmbeddedCollection<M, T, R> {

    private final R parent;
    private final Set<T> set;
    private final Class<T> cls;

    public EmbeddedCodeSetImpl(R parent, Set<T> set, Class<T> cls) {
        this.parent = parent;
        this.set = set;
        this.cls = cls;
    }

    @Override
    public EmbeddedCollection<M, T, R> add(T value) {
        set.add(value);
        return this;
    }

    @Override
    public M add() {
        T value = CodeFactory.create(cls);
        set.add(value);
        return CodeFactory.modify(this, value);
    }

    @Override
    public R and() {
        return parent;
    }
}
