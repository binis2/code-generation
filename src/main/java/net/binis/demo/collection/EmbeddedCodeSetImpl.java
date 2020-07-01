package net.binis.demo.collection;

import java.util.Comparator;
import java.util.Set;

public class EmbeddedCodeSetImpl<M, T, R> extends EmbeddedCodeCollectionImpl<M, T, R> {

    public EmbeddedCodeSetImpl(R parent, Set<T> set, Class<T> cls) {
        super(parent, set, cls);
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> sort(Comparator<? super T> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public M get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public M insert(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public M first() {
        throw new UnsupportedOperationException();
    }

    @Override
    public M last() {
        throw new UnsupportedOperationException();
    }

}
