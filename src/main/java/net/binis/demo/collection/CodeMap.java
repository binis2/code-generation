package net.binis.demo.collection;

public interface CodeMap<K, V, R> {

    CodeMap<K, V, R> put(K key, V value);
    R and();

}
