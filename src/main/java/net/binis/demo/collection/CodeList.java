package net.binis.demo.collection;

public interface CodeList<T, R> {

    CodeList<T, R> add(T value);
    R and();

}
