package net.binis.demo.factory;

public class CodeFactory {

    public static <T> T create(Class<T> cls) {
        try {
            return cls.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <M, T> M modify(T value) {
        return null;
    }
}
