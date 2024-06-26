package net.binis.codegen.other;

import net.binis.codegen.annotation.CodePrototype;
import net.binis.codegen.annotation.DefaultString;
import net.binis.codegen.TestPrototype;

import java.util.List;
import java.util.Optional;

import static java.util.Comparator.comparing;

@CodePrototype
public interface ReferencePrototype {

    String title();

    default Optional<TestPrototype> getOptional() {
        return Optional.empty();
    }

    default String getStr(TestPrototype proto) {
        return proto.title();
    }

    default public List<TestPrototype> getSorted(List<TestPrototype> list) {
        return list.stream().sorted(comparing(TestPrototype::title)).toList();
    }

    default boolean _equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var other = (ReferencePrototype) o;

        return title().equals(other.title());
    }

    default int _hashCode() {
        return title().hashCode();
    }
}