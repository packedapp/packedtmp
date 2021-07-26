package app.packed.bean;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import app.packed.build.BuildWirelets;
import app.packed.component.Wirelet;

class BeanWirelets {

    // provide

    /**
     * Returns a spying wirelet that will perform the specified action every time a bean has been wired. This is similar to
     * {@link BuildWirelets#spyOnWire(Consumer)}, however the action is only performed on components that are beans.
     * <p>
     * This wirelet can only be specified when building
     * 
     * @param action
     *            the action to perform
     * @return the wirelet
     */
    // Tror faktisk maaske den default whole App for app, Container for container, Component for Component
    // Hvis vi har en der tager class BeanMirro syntes ikke vi behoever den her...
    public static Wirelet spyOnBeanWire(Consumer<? super BeanMirror> action) {
        requireNonNull(action, "action is null");

        BuildWirelets.spyOnWire(BeanMirror.class, e -> System.out.println("Installing bean of type " + e.beanType()));

        return BuildWirelets.spyOnWire(m -> {
            if (m instanceof BeanMirror bm) {
                action.accept(bm);
            }
        });
    }
}
