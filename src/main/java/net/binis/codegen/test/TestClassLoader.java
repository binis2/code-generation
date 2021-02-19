package net.binis.codegen.test;

public class TestClassLoader extends ClassLoader {

    public Class<?> define(String name, JavaByteObject byteObject) throws ClassFormatError {
        byte[] bytes = byteObject.getBytes();
        return defineClass(name, bytes, 0, bytes.length);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

}
