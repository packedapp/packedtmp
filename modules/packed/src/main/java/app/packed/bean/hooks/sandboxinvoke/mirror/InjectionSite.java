package app.packed.bean.hooks.sandboxinvoke.mirror;

import java.lang.reflect.Member;
import java.util.List;

import app.packed.bean.BeanMirror;
import app.packed.inject.service.ServiceRegistry;

// Constructor/Executable/Field/Injectable Function

// Altsaa er det et mirror??? Vi har jo direkte adgang til parent? taenker jeg, eller maaske ikke

// Hmm har man noget roles istedet for ConsumerSite, ProducerSite..
// Det er jo klart man kan vaere begge dele
// Altsaa InjectionSite er jo en slags node.
// Hvor dependencies'ene er edges
// Eller hva, er det en anden API

// Er altid en operation...

public /* sealed */ interface InjectionSite /* permits ConsumerSite,ProducerSite */ {

    /** {@return the services that are available at this injection site.} */
    ServiceRegistry availableServices(); // Er vel tom for disabled injection

    /** {@return the bean this injection site is a part of.} */
    BeanMirror bean();

    /** {@return the dependencies of this injection site.} */
    List<Dependency> dependencies();

    /** {@return the number of dependencies for this injection site.} */
    int dependencyCount();

    /** {@return whether or not this injection site has any dependencies.} */
    default boolean hasDependencies() {
        return dependencyCount() == 0;
    }

    /**
     * Returns whether or not all dependencies have been {@link Dependency#isSatisfiable()}.
     * 
     * @return
     */
    boolean isSatisfiable();

    // Hmm.. Hvad hvis det er et fake factory
    // Factory = FunctionalInterfaceWrapper, Reflection-Member, MethodHandle, Transformed
    //// For bean hooks->ReflectionMember
    //// For bean initializer -> Any
    Member member();
}
