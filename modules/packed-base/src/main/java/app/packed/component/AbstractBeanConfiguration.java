package app.packed.component;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.BeanSetup;

public abstract /* non-sealed */ class AbstractBeanConfiguration extends ComponentConfiguration {

    /** */
    private BeanSetup bean;

    // Her kan en extension faktisk exporte ting...
    protected <T> ExportedServiceConfiguration<T> exportAsService() {
        return bean.sourceExport();
    }

    protected void provideAsService() {
        bean.sourceProvide();
    }

    protected void provideAsService(Key<?> key) {
        bean.sourceProvideAs(key);
    }

    protected Optional<Key<?>> sourceProvideAsKey() {
        return bean.sourceProvideAsKey();
    }
}
