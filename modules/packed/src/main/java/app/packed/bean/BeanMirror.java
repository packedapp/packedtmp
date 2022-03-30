package app.packed.bean;

import java.util.Collection;
import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.bean.operation.OperationMirror;
import app.packed.bean.operation.lifecycle.BeanFactoryOperationMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import packed.internal.bean.BeanSetup.BuildTimeBeanMirror;

// Class -> members
// Scanning class -> Hooks
// Bean -> Operation
/**
 * A mirror of a single bean.
 */
public sealed interface BeanMirror extends ComponentMirror permits BuildTimeBeanMirror {

    /**
     * Returns the type (class) of the bean. If the bean does not have a proper class, for example, a functional bean.
     * {@code void.class} is returned.
     * 
     * @return the type (class) of the bean.
     */
    Class<?> beanClass();

    /** {@return the kind of the bean.} */
    BeanKind beanKind();

    /** {@return the container the bean belongs to. Is identical to #parent() which is always present for a bean.} */
    ContainerMirror container();

    default Optional<BeanFactoryOperationMirror> factory() {
        // Kunne man forstille sig at en bean havde 2 constructors??
        // Som man valgte af paa runtime????
        return Optional.empty();
    }

    default Class<? extends Extension<?>> installedVia() {
        // The extension that performed the actual installation of the bean
        return BeanExtension.class;
    }

    /** {@return a collection of all of the operations declared by the bean.} */
    default Collection<OperationMirror> operations() {
        throw new UnsupportedOperationException();
    }

    default Collection<OperationMirror> operations(boolean includeSynthetic) {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a collection of all of the operations declared by the bean of the specified type.
     * 
     * @param <T>
     * @param operationType
     *            the type of operations to include
     * @return a collection of all of the operations declared by the bean of the specified type.
     */
    default <T extends OperationMirror> Collection<T> operations(Class<T> operationType) {
        throw new UnsupportedOperationException();
    }
}

interface Zandbox {
    /**
     * @return
     */
    ApplicationMirror application();

}
// boolean isInstantiated

// Scope-> BuildConstant, RuntimeConstant, Prototype...

// Class<?> source() Object.class, Factory.Class, Class.class maaske en enum... Maaske noget andet

///** {@return all hooks that have been applied on the bean.} */
//// Tror slet ikke vi skal have dem her...
//Set<BeanElementMirror> hooks();
//
//<T extends BeanElementMirror> Set<?> hooks(Class<T> hookType);

// Maaske er den bare fyldt med attributer istedet for et decideres mirror IDK
// Er ikke sikker paa en driver har et mirror
// kan bare vaere driverX paa component beanen
//interface ZBeanDriverMirror {}
// Den er cool, men ikke super brugbar...
// I det fleste tilfaede er jeg komplet ligeglad med om det er factory eller class der laver noget
// Hellere hvad den skal bruges til/lifetime
//enum BeanOldMode {
//    CLASS_MANY,
//    /** */
//    CLASS_NONE, CLASS_ONE, FACTORY_MANY, FACTORY_ONE,
//    /** User provides a single instance. */
//    INSTANCE;
//
//    boolean isClass() {
//        return this == CLASS_NONE || this == CLASS_ONE || this == CLASS_MANY;
//    }
//
//    boolean isFactory() {
//        return this == FACTORY_ONE || this == FACTORY_MANY;
//    }
//
//    // Maaske rename INSTANCE= Object and then have instance
//    boolean isInstances() {
//        return this != CLASS_NONE;
//    }
//
//    boolean isMany() {
//        return this == CLASS_MANY || this == FACTORY_MANY;
//    }
//
//    boolean isNone() {
//        return this == CLASS_NONE;
//    }
//
//    boolean isOne() {
//        return this == INSTANCE || this == CLASS_ONE || this == FACTORY_ONE;
//    }
//
//    // Maybe array we
//
//    static Set<BeanOldMode> allClass() {
//        return Set.of(CLASS_NONE, CLASS_ONE, CLASS_MANY);
//    }
//
//    static Set<BeanOldMode> allFactory() {
//        return Set.of(FACTORY_ONE, FACTORY_MANY);
//    }
//
//    static Set<BeanOldMode> allInstances() {
//        throw new UnsupportedOperationException();
//    }
//
//    static Set<BeanOldMode> allOne() {
//        throw new UnsupportedOperationException();
//    }
//}
