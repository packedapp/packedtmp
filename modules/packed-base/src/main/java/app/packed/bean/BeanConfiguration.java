package app.packed.bean;

import java.util.concurrent.Callable;

import app.packed.component.ComponentConfiguration;

public abstract non-sealed class BeanConfiguration<T> extends ComponentConfiguration {

    /**
     * This method can be overridden to return a subclass of bean mirror.
     * 
     * {@inheritDoc}
     */
    @Override
    protected BeanMirror mirror() {
        throw new UnsupportedOperationException();
    }

    public <X extends Runnable & Callable<String>> X foo() {
        return null;
    }
}
