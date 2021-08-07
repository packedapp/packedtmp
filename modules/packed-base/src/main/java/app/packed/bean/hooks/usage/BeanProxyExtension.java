package app.packed.bean.hooks.usage;

import app.packed.bean.ApplicationBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.Factory;

// Syntes service interfaces er noget andet... end bean Proxy

class BeanProxyExtension extends Extension {
    BeanProxyExtension() {}

    // Problemet er her provide()
    // Vi skal maaske have en provide(ApplicationBeanConfiguration)

    // Installs what looks like an ApplicationBean. But is lazily initialized and started
    public <T> ApplicationBeanConfiguration<T> installLazy(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    public <T> ApplicationBeanConfiguration<T> installLazy(Factory<T> clazz) {
        throw new UnsupportedOperationException();
    }
}
