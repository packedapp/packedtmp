package app.packed.component;

import java.util.Optional;
import java.util.Set;

import app.packed.container.Extension;

/**
 * A mirror of a bean (component).
 */
public interface BeanMirror extends ComponentMirror {

    /** {@return the type (class) of the bean.} */
    Class<?> beanType();

    /**
     * Returns any extension the driver is part of. All drivers are either part of an extension. Or is a build in drive
     * 
     * @return any extension the driver is part of
     */
    Optional<Class<? extends Extension>> driverExtension();

    boolean hasBeen(); // support Hooks...

    /** {@return all hooks that have been applied on the bean.} */
    Set<?> hooks();

    <T /* extends HookMirror */> Set<?> hooks(Class<T> hookType);

    BeanMode mode();
}

// Maaske er den bare fyldt med attributer istedet for et decideres mirror IDK
interface ZBeanDriverMirror {

    Optional<Class<? extends Extension>> extension();

    /** {@return } */
    boolean introspect();

    // FunctionClass, FunctionType

    Set<BeanMode> modes(); // Object.class, Class.class, Factory.class
}

class ZlassComponentDriverBuilder {

    //// For instantiationOnly
    // reflectOnConstructorOnly();

    // reflectOn(Fields|Methods|Constructors)
    // look in declaring class
}