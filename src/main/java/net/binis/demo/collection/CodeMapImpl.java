package net.binis.demo.collection;

import java.util.Map;

public class CodeMapImpl<K, V, R> implements CodeMap<K, V, R> {

    private final R parent;
    private final Map<K, V> map;

    public CodeMapImpl(R parent, Map<K, V> map) {
        this.parent = parent;
        this.map = map;
    }

    @Override
    public CodeMap<K, V, R> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    @Override
    public R and() {
        return parent;
    }
}
