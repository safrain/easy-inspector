package me.safrain.validator.expression;

import java.util.Iterator;

public abstract class TransformingIterator<T, E> implements Iterator<E> {

    private Iterator<T> iterator;

    public TransformingIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public E next() {
        return transform(iterator.next());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    protected abstract E transform(T input);
}
