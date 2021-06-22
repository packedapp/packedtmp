package app.packed.application;

import app.packed.container.BaseContainerConfiguration;
import app.packed.container.ContainerDriver;

public abstract class BaseApplicationAssembly<A> extends Application2Assembly<A, BaseContainerConfiguration> {

    protected BaseApplicationAssembly(ApplicationDriver<A> driver) {
        super(driver, ContainerDriver.of());
    }
}
