package app.packed.component;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.bean.BeanSetup;

public abstract /* non-sealed */ class BeanConfiguration extends ComponentConfiguration {

    
    BeanSetup bean() {
        return (BeanSetup) component();
    }
    // Her kan en extension faktisk exporte ting...
    protected <T> ExportedServiceConfiguration<T> exportAsService() {
        return bean().sourceExport();
    }

    @Override
    protected BeanMirror mirror() {
        throw new UnsupportedOperationException();
    }

    protected void provideAsService() {
        bean().sourceProvide();
    }

    protected void provideAsService(Key<?> key) {
        bean().sourceProvideAs(key);
    }

    protected Optional<Key<?>> sourceProvideAsKey() {
        return bean().sourceProvideAsKey();
    }
}
