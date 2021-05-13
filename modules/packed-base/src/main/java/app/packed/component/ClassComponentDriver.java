package app.packed.component;

import java.util.Set;

import app.packed.inject.Factory;

public interface ClassComponentDriver<T, C extends ComponentConfiguration> {

    /**
     * @param instance
     * @return
     * @throws UnsupportedOperationException
     *             if class
     */
    ComponentDriver<C> bind(Class<? extends T> implementation);

    ComponentDriver<C> bind(Factory<? extends T> factory);

    /**
     * @param instance
     *            the instance to bind
     * @return a component driver that wraps this driver and the specified instance
     * @throws UnsupportedOperationException
     *             if instance binding is not supported
     */
    ComponentDriver<C> bindInstance(T instance);

    /** {@return a set containing all modes this driver supports. } */
    Set<? extends ClassComponentMode> supportedModes();
    
    ClassComponentDriver<T, C> with(Wirelet... wirelet);
}
