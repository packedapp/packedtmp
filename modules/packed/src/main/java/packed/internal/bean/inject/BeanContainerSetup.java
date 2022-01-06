package packed.internal.bean.inject;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;

import app.packed.base.Nullable;
import app.packed.inject.service.ServiceExtension;
import packed.internal.container.ContainerSetup;
import packed.internal.inject.service.ServiceManagerSetup;

public final class BeanContainerSetup {

    /** All dependants that needs to be resolved. */
    public final ArrayList<InjectionNode> nodes = new ArrayList<>();

    private final ContainerSetup container;

    /** A service manager that handles everything to do with services, is lazily initialized. */
    @Nullable
    private ServiceManagerSetup sm;

    public BeanContainerSetup(ContainerSetup container) {
        this.container = requireNonNull(container);
    }

    /**
     * Adds the specified injectable to list of injectables that needs to be resolved.
     * 
     * @param dependant
     *            the injectable to add
     */
    public void addNode(InjectionNode dependant) {
        nodes.add(requireNonNull(dependant));

        // Bliver noedt til at lave noget sidecar preresolve her.
        // I virkeligheden vil vi bare gerne checke at om man
        // har ting der ikke kan resolves via contexts
        if (sm == null && !dependant.dependencies.isEmpty()) {
            container.useExtension(ServiceExtension.class);
        }
    }

    public void resolve() {
        // Resolve local services
        if (sm != null) {
            sm.prepareDependants(container);
        }

        for (InjectionNode i : nodes) {
            i.resolve(sm);
        }

        // Now we know every dependency that we are missing
        // I think we must plug this in somewhere

        if (sm != null) {
            sm.dependencies().checkForMissingDependencies(container);
            sm.close(container, container.lifetime.pool);
        }
        // TODO Check any contracts we might as well catch it early
    }

    @Nullable
    public ServiceManagerSetup getServiceManager() {
        return sm;
    }

    public ServiceManagerSetup getServiceManagerOrCreate() {
        ServiceManagerSetup s = sm;
        if (s == null) {
            container.useExtension(ServiceExtension.class);
            s = sm;
        }
        return s;
    }

    /**
     * This method is invoked from the constructor of a {@link ServiceExtension} to create a new
     * {@link ServiceManagerSetup}.
     * 
     * @return the new service manager
     */
    public ServiceManagerSetup newServiceManagerFromServiceExtension() {
        return sm = new ServiceManagerSetup(sm);
    }

}
