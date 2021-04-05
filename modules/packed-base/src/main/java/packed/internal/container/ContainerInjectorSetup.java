package packed.internal.container;

import java.util.ArrayList;

import packed.internal.inject.dependency.InjectionNode;

public class ContainerInjectorSetup {

    /** All dependants that needs to be resolved. */
    public final ArrayList<InjectionNode> nodes = new ArrayList<>();
}
