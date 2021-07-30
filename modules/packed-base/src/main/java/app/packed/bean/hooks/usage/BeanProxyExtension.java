package app.packed.bean.hooks.usage;

import app.packed.bean.BeanConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.Factory;

// Syntes service interfaces er noget andet... end bean Proxy

class BeanProxyExtension extends Extension {
    BeanProxyExtension() {}

    // Problemet er her provide()
    // Vi skal maaske have en provide(ApplicationBeanConfiguration)

    // Installs what looks like an ApplicationBean. But is lazily initialized and started
    /* Application */BeanConfiguration<?> installLazy(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    /* Application */BeanConfiguration<?> installLazy(Factory<?> clazz) {
        throw new UnsupportedOperationException();
    }
}
