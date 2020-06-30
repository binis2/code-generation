package net.binis.demo.collection;

import net.binis.demo.factory.CodeFactory;

import java.util.List;

public class EmbeddedCodeListImpl<M, T, R> implements EmbeddedCodeList<M, T, R> {

    private final R parent;
    private final List<T> list;
    private final Class<T> cls;

    public EmbeddedCodeListImpl(R parent, List<T> list, Class<T> cls) {
        this.parent = parent;
        this.list = list;
        this.cls = cls;
    }

    @Override
    public EmbeddedCodeList<M, T, R> add(T value) {
        list.add(value);
        return this;
    }

    @Override
    public M add() {
        T value = CodeFactory.create(cls);
        list.add(value);
        return CodeFactory.modify(this, value);
    }

    @Override
    public R and() {
        return parent;
    }
}
