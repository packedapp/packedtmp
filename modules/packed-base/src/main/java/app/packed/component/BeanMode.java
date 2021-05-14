package app.packed.component;

import java.util.Set;

// Den er cool, men ikke super brugbar...
public enum BeanMode {
    /** User provides a single instance. */
    INSTANCE,
    /** */
    CLASS_NONE, CLASS_ONE, CLASS_MANY, FACTORY_ONE, FACTORY_MANY;

    boolean isClass() {
        return this == CLASS_NONE || this == CLASS_ONE || this == CLASS_MANY;
    }

    boolean isFactory() {
        return this == FACTORY_ONE || this == FACTORY_MANY;
    }

    boolean isMany() {
        return this == CLASS_MANY || this == FACTORY_MANY;
    }

    boolean isNone() {
        return this == CLASS_NONE;
    }

    // Maaske rename INSTANCE= Object and then have instance
    boolean isInstances() {
        return this != CLASS_NONE;
    }

    boolean isOne() {
        return this == INSTANCE || this == CLASS_ONE || this == FACTORY_ONE;
    }

    // Maybe array we

    static Set<BeanMode> allInstances() {
        throw new UnsupportedOperationException();
    }

    static Set<BeanMode> allOne() {
        throw new UnsupportedOperationException();
    }

    static Set<BeanMode> allClass() {
        return Set.of(CLASS_NONE, CLASS_ONE, CLASS_MANY);
    }

    static Set<BeanMode> allFactory() {
        return Set.of(FACTORY_ONE, FACTORY_MANY);
    }
}
