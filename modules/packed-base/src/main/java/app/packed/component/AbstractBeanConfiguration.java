package app.packed.component;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.component.BeanSetup;

public abstract /* non-sealed */ class AbstractBeanConfiguration extends ComponentConfiguration {

    private BeanSetup bean;

    protected <T> ExportedServiceConfiguration<T> sourceExport() {
        return bean.sourceExport();
    }

    protected void sourceProvide() {
        bean.sourceProvide();
    }

    protected void sourceProvideAs(Key<?> key) {
        bean.sourceProvideAs(key);
    }

    protected Optional<Key<?>> sourceProvideAsKey() {
        return bean.sourceProvideAsKey();
    }
}
