package app.packed.bean;

import java.util.Collection;
import java.util.Optional;

import app.packed.bean.mirror.BeanOperationMirror;
import app.packed.bean.operation.BeanFactoryMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.extension.Extension;
import packed.internal.bean.BeanSetup.BuildTimeBeanMirror;

// Class -> members
// Scanning class -> Hooks
// Bean -> Operation
/**
 * A mirror of a bean.
 */
public sealed interface BeanMirror extends ComponentMirror permits BuildTimeBeanMirror {

    /** {@return the container the bean belongs to. Is identical to #parent() which is never empty for a bean.} */
    ContainerMirror container();

    default Class<? extends Extension<?>> installedVia() {
        // The extension that performed the actual installation of the bean
        return BeanExtension.class;
    }

    /** {@return the type (class) of the bean.} */
    // Optional instead??? Nope, vi returnere void.class
    // What about an akka actor???
    Class<?> beanClass(); // What does a SyntheticBean return??? Object.class, Synthetic.class, Void.class, void.class

    /** {@return the kind of the bean.} */
    BeanKind beanKind();

    default Optional<BeanFactoryMirror> factory() {
        // Kunne man forstille sig at en bean havde 2 constructors??
        // Som man valgte af paa runtime????
        return Optional.empty();
    }

    default Collection<BeanOperationMirror> operations() {
        throw new UnsupportedOperationException();
    }

    default <T extends BeanOperationMirror> Collection<T> operations(Class<T> operationType) {
        throw new UnsupportedOperationException();
    }
    // boolean isInstantiated

    // Scope-> BuildConstant, RuntimeConstant, Prototype...

    // Class<?> source() Object.class, Factory.Class, Class.class maaske en enum... Maaske noget andet
}
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
