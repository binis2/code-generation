package net.binis.demo.collection;

import net.binis.demo.factory.CodeFactory;

import java.util.Comparator;
import java.util.List;

public class EmbeddedCodeListImpl<M, T, R> extends EmbeddedCodeCollectionImpl<M, T, R> {

    private final List<T> list;

    public EmbeddedCodeListImpl(R parent, List<T> list, Class<T> cls) {
        super(parent, list, cls);
        this.list = list;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> remove(int index) {
        list.remove(index);
        return this;
    }

    @Override
    public EmbeddedCodeCollection<M, T, R> sort(Comparator<? super T> comparator) {
        list.sort(comparator);
        return this;
    }

    @Override
    public M get(int index) {
        return CodeFactory.modify(this, list.get(index));
    }

    @Override
    public M insert(int index) {
        T value = CodeFactory.create(cls);
        list.add(index, value);
        return CodeFactory.modify(this, value);
    }

    @Override
    public M first() {
        return CodeFactory.modify(this, list.get(0));
    }

    @Override
    public M last() {
        return CodeFactory.modify(this, list.get(list.size() - 1));
    }
}
