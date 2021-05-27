package app.packed.component;

import java.util.Optional;
import java.util.Set;

import app.packed.component.BeanMirror.BeanOldMode;
import app.packed.container.Extension;

/**
 * A mirror of a bean (component).
 */
public interface BeanMirror extends ComponentMirror {

    /** {@return the type (class) of the bean.} */
    Class<?> beanType();

    /**
     * Returns any extension the bean's driver is part of. All drivers are either part of an extension. Or is a build in
     * drive
     * 
     * @return any extension the bean's driver is part of
     */
    // Tror maaske den skal op paa ComponentMirror... 
    // Ved ikke om vi kan definere end ContainerDriver for en extension???
    // Det primaere er vel injection
    Optional<Class<? extends Extension>> driverExtension();

    /** {@return all hooks that have been applied on the bean.} */
    Set<?> hooks();

    <T /* extends HookMirror */> Set<?> hooks(Class<T> hookType);

    BeanOldMode mode();

    
    // Den er cool, men ikke super brugbar...
    // I det fleste tilfaede er jeg ligeglad med om det er factory eller class
    // Hellere hvad den skal bruges til
    public enum BeanOldMode {
        CLASS_MANY,
        /** */
        CLASS_NONE, CLASS_ONE, FACTORY_MANY, FACTORY_ONE,
        /** User provides a single instance. */
        INSTANCE;

        boolean isClass() {
            return this == CLASS_NONE || this == CLASS_ONE || this == CLASS_MANY;
        }

        boolean isFactory() {
            return this == FACTORY_ONE || this == FACTORY_MANY;
        }

        // Maaske rename INSTANCE= Object and then have instance
        boolean isInstances() {
            return this != CLASS_NONE;
        }

        boolean isMany() {
            return this == CLASS_MANY || this == FACTORY_MANY;
        }

        boolean isNone() {
            return this == CLASS_NONE;
        }

        boolean isOne() {
            return this == INSTANCE || this == CLASS_ONE || this == FACTORY_ONE;
        }

        // Maybe array we

        static Set<BeanOldMode> allClass() {
            return Set.of(CLASS_NONE, CLASS_ONE, CLASS_MANY);
        }

        static Set<BeanOldMode> allFactory() {
            return Set.of(FACTORY_ONE, FACTORY_MANY);
        }

        static Set<BeanOldMode> allInstances() {
            throw new UnsupportedOperationException();
        }

        static Set<BeanOldMode> allOne() {
            throw new UnsupportedOperationException();
        }
    }

}

// Maaske er den bare fyldt med attributer istedet for et decideres mirror IDK
interface ZBeanDriverMirror {

    Set<BeanOldMode> modes(); // Object.class, Class.class, Factory.class
}

class ZlassComponentDriverBuilder {

    //// For instantiationOnly
    // reflectOnConstructorOnly();

    // reflectOn(Fields|Methods|Constructors)
    // look in declaring class
}