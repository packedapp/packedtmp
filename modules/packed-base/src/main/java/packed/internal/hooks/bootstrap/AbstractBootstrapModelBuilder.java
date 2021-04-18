package packed.internal.hooks.bootstrap;

import java.util.WeakHashMap;
import java.util.concurrent.locks.ReentrantLock;

abstract class AbstractBootstrapModelBuilder {

    /** A map that contains Bootstrap, Builder or Throwable */
    static final WeakHashMap<Class<?>, Object> DATA = new WeakHashMap<>();

    /** A lock used for making sure that we only load one extension (and its dependencies) at a time. */
    static final ReentrantLock GLOBAL_LOCK = new ReentrantLock();
}
