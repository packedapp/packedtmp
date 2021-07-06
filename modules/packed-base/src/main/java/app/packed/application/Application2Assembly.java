package app.packed.application;

import app.packed.component.Assembly;
import app.packed.component.ComponentDriver;
import app.packed.container.ContainerConfiguration;

public abstract non-sealed class Application2Assembly<A, C extends ContainerConfiguration> extends Assembly<C> implements ApplicationProducer<A> {

    protected Application2Assembly(ApplicationDriver<A> adriver, ComponentDriver<? extends C> driver) {
        super(driver);
    }
}
