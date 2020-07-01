package net.binis.demo.collection;

public interface EmbeddedCollection<M, T, R> {

    EmbeddedCollection<M, T, R> add(T value);
    M add();
    R and();

}
