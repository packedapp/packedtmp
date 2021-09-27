package app.packed.bean;

import java.util.Set;

import app.packed.component.ComponentMirror;

/**
 * A mirror of a bean (component).
 */
public non-sealed interface BeanMirror extends ComponentMirror {

    /** {@return the type (class) of the bean.} */
    Class<?> beanType();

    /** {@return all hooks that have been applied on the bean.} */
    Set<?> hooks();

    <T /* extends HookMirror */> Set<?> hooks(Class<T> hookType);

    /** {@return the kind of the bean.} */
    BeanKind kind();

    // boolean isInstantiated
    // Class<?> source() Object.class, Factory.Class, Class.class maaske en enum... Maaske noget andet
}

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
