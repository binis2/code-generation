package net.binis.demo.collection;

public interface CodeSet<T, R> {

    CodeSet<T, R> add(T value);
    R and();

}
