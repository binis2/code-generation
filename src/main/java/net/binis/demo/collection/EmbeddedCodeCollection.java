package net.binis.demo.collection;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface EmbeddedCodeCollection<M, T, R> {

    EmbeddedCodeCollection<M, T, R> add(T value);

    EmbeddedCodeCollection<M, T, R> remove(T value);

    EmbeddedCodeCollection<M, T, R> remove(int index);

    EmbeddedCodeCollection<M, T, R> clear();

    EmbeddedCodeCollection<M, T, R> each(Consumer<M> doWhat);

    EmbeddedCodeCollection<M, T, R> ifEmpty(Consumer<EmbeddedCodeCollection<M, T, R>> doWhat);

    EmbeddedCodeCollection<M, T, R> ifNotEmpty(Consumer<EmbeddedCodeCollection<M, T, R>> doWhat);

    EmbeddedCodeCollection<M, T, R> ifContains(T value, Consumer<EmbeddedCodeCollection<M, T, R>> doWhat);

    EmbeddedCodeCollection<M, T, R> ifNotContains(T value, Consumer<EmbeddedCodeCollection<M, T, R>> doWhat);

    EmbeddedCodeCollection<M, T, R> sort(Comparator<? super T> comparator);

    M add();

    M get(int index);

    M insert(int index);

    M first();

    M last();

    M find(Predicate<T> predicate);

    R and();

}
