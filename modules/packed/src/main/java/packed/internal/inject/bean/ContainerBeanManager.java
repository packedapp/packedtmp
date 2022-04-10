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


    public void resolve() {
        // Resolve local services
        if (sm != null) {
            sm.prepareDependants();
        }

        for (DependencyNode i : sm.consumers) {
            i.resolve(sm);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sm != null) {
            sm.ios.requirementsOrCreate().checkForMissingDependencies();
            sm.close();
        }
        // TODO Check any contracts we might as well catch it early
    }

}
