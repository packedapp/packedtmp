package packed.internal.container;

import java.util.ArrayList;

import packed.internal.inject.dependency.DependancyConsumer;

public class ContainerInjectorSetup {

    /** All dependants that needs to be resolved. */
    public final ArrayList<DependancyConsumer> dependants = new ArrayList<>();
}
