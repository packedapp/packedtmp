package app.packed.application.host;

import app.packed.component.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;

public abstract non-sealed class ApplicationAssembly<C extends ContainerConfiguration> extends Assembly<C> {

    protected ApplicationAssembly(ContainerDriver<? extends C> driver) {
        super(driver);
    }

}
