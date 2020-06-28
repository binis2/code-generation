package net.binis.demo.collection;

import java.util.Set;

public class CodeSetImpl<T, R> implements CodeSet<T, R> {

    private final R parent;
    private final Set<T> set;

    public CodeSetImpl(R parent, Set<T> set) {
        this.parent = parent;
        this.set = set;
    }

    @Override
    public CodeSet<T, R> add(T value) {
        set.add(value);
        return this;
    }

    @Override
    public R and() {
        return parent;
    }
}
