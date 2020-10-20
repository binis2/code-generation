package net.binis.demo.modifier;

@FunctionalInterface
public interface Modifiable<T> {
    T with();
}
