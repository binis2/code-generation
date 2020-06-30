package net.binis.demo.collection;

public interface EmbeddedCodeList<M, T, R> {

    EmbeddedCodeList<M, T, R> add(T value);
    M add();
    R and();

}
