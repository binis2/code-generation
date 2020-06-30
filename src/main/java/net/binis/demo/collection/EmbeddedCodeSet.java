package net.binis.demo.collection;

public interface EmbeddedCodeSet<M, T, R> {

    EmbeddedCodeSet<M, T, R> add(T value);
    M add();
    R and();

}
