package net.binis.demo.collection;

import java.util.List;

public class CodeListImpl<T, R> implements CodeList<T, R> {

    private final R parent;
    private final List<T> list;

    public CodeListImpl(R parent, List<T> list) {
        this.parent = parent;
        this.list = list;
    }

    @Override
    public CodeList<T, R> add(T value) {
        list.add(value);
        return this;
    }

    @Override
    public R and() {
        return parent;
    }
}
