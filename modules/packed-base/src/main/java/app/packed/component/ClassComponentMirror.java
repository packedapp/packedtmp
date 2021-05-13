package app.packed.component;

import java.util.Optional;
import java.util.Set;

import app.packed.container.Extension;

public interface ClassComponentMirror {

    /**
     * Returns any extension the driver is part of. All drivers are either part of an extension. Or is a build in drive
     * 
     * @return any extension the driver is part of
     */
    Optional<Class<? extends Extension>> driverExtension();

    boolean isSourceReflected(); // support Hooks...

    ClassComponentMode mode();

    /**
     * Returns the type (class) of the source.
     * 
     * if instance it is .getClass()
     * 
     * if class it is (surprise) the class
     * 
     * if factory it is the returning class
     * 
     * @return
     */
    Class<?> sourceType();
}

// Maaske er den bare fyldt med attributer istedet for et decideres mirror IDK
interface ClassZComponentDriverMirror {

    Optional<Class<? extends Extension>> extension();

    Set<ClassComponentMode> modes(); // Object.class, Class.class, Factory.class

    // FunctionClass, FunctionType

    /** {@return } */
    boolean reflectOnSource();
}

class ZlassComponentDriverBuilder {

    //// For instantiationOnly
    // reflectOnConstructorOnly();

    // reflectOn(Fields|Methods|Constructors)
    // look in declaring class
}