package packed.internal.inject.bean;

import app.packed.base.Nullable;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.service.ContainerInjectionManager;

/** This class is responsible for managing all beans in a container. */
public final class ContainerBeanManager {


    /** A service manager that handles everything to do with services, is lazily initialized. */
    public final ContainerInjectionManager sm;

    @Nullable
    private final ContainerBeanManager parent;

    public ContainerBeanManager(ContainerSetup container, @Nullable ContainerBeanManager parent) {
        this.parent = parent;
        this.sm = new ContainerInjectionManager(container, null);
    }


    public ContainerInjectionManager getServiceManager() {
        return sm;
    }


}
