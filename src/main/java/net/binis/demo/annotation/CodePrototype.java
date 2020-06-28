package net.binis.demo.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@CodeAnnotation
@Retention(RetentionPolicy.RUNTIME)
public @interface CodePrototype {

    String name() default "";
    boolean generateInterface() default true;
    boolean generateModifier() default false;
    boolean base() default false;
    Class<?> baseModifierClass() default void.class;
    Class<?> mixInClass() default void.class;

}
