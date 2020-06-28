package net.binis.demo.tools;

import lombok.ToString;

@ToString
public class Holder<T> {

    private T object;

    public Holder(T object) {
        super();
        this.object = object;
    }

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }

    public static <T> Holder<T> of(T object) {
        return new Holder<>(object);
    }

    public static <T> Holder<T> blank() {
        return new Holder<>(null);
    }

}
