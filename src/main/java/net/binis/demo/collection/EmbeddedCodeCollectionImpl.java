package net.binis.demo.collection;

import net.binis.demo.factory.CodeFactory;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class EmbeddedCodeCollectionImpl<M, T, R> implements EmbeddedCodeCollection<M, T, R> {

    private final Collection<T> collection;
    protected final R parent;
    protected final Class<T> cls;

    public EmbeddedCodeCollectionImpl(R parent, Collection<T> collection, Class<T> cls) {
        this.parent = parent;
        this.collection = collection;
        this.cls = cls;
    }


    @Override
    public EmbeddedCodeCollection<M, T, R> add(T value) {
        collection.add(value);
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> remove(T value) {
        collection.remove(value);
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> clear() {
        collection.clear();
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> each(Consumer<M> doWhat) {
        collection.forEach(e -> doWhat.accept(CodeFactory.modify(this, e)));
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> ifEmpty(Consumer<EmbeddedCodeCollection<M, T, R>> doWhat) {
        if (collection.isEmpty()) {
            doWhat.accept(this);
        }
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> ifNotEmpty(Consumer<EmbeddedCodeCollection<M, T, R>> doWhat) {
        if (!collection.isEmpty()) {
            doWhat.accept(this);
        }
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> ifContains(T value, Consumer<EmbeddedCodeCollection<M, T, R>> doWhat) {
        if (collection.contains(value)) {
            doWhat.accept(this);
        }
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> ifNotContains(T value, Consumer<EmbeddedCodeCollection<M, T, R>> doWhat) {
        if (!collection.contains(value)) {
            doWhat.accept(this);
        }
        return this;
    }

    @Override
    public M add() {
        T value = CodeFactory.create(cls);
        collection.add(value);
        return CodeFactory.modify(this, value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public M find(Predicate<T> predicate) {
        return collection.stream().filter(predicate).map(e -> (M) CodeFactory.modify(this, e)).findFirst().orElseThrow();
    }

    @Override
    public R and() {
        return parent;
    }
}
