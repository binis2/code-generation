package net.binis.codegen.tools;

import lombok.SneakyThrows;

public class Reflection {

    private Reflection() {

    }

    @SneakyThrows
    public static <T> T instantiate(Class<T> cls) {
        return cls.getDeclaredConstructor().newInstance();
    }
}
