package packed.internal.bean.inject;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.inject.service.ServiceExtension;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.service.ContainerInjectionManager;

/** This class is responsible for managing all beans in a container. */
public final class ContainerBeanManager {

    /** All dependants that needs to be resolved. */
    public final ArrayList<DependencyNode> consumers = new ArrayList<>();

    /** The container that beans are managed from. */
    private final ContainerSetup container;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ContainerInjectionManager sm;

    @Nullable
    private final ContainerBeanManager parent;

    public ContainerBeanManager(ContainerSetup container, @Nullable ContainerBeanManager parent) {
        this.container = requireNonNull(container);
        this.parent = parent;
        //sm = new ContainerInjectionManager(parent == null ? null : parent.sm);
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addConsumer(DependencyNode dependant) {
        consumers.add(requireNonNull(dependant));

        // Bliver noedt til at lave noget sidecar preresolve her.
        // I virkeligheden vil vi bare gerne checke at om man
        // har ting der ikke kan resolves via contexts
        if (sm == null && !dependant.dependencies.isEmpty()) {
            container.useExtension(ServiceExtension.class);
        }
    }

    @Nullable
    public ContainerInjectionManager getServiceManager() {
        return sm;
    }

    /**
     * This method is invoked from the constructor of a {@link ServiceExtension} to create a new
     * {@link ContainerInjectionManager}.
     * 
     * @return the new service manager
     */
    public ContainerInjectionManager newServiceManagerFromServiceExtension() {
        return sm = new ContainerInjectionManager(sm);
    }

    public void resolve() {
        // Resolve local services
        if (sm != null) {
            sm.prepareDependants(container);
        }

        for (DependencyNode i : consumers) {
            i.resolve(sm);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sm != null) {
            sm.requirements().checkForMissingDependencies(container);
            sm.close(container, container.lifetime.pool);
        }
        // TODO Check any contracts we might as well catch it early
    }

}
