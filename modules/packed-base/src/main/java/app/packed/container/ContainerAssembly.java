package app.packed.container;

import app.packed.component.Assembly;
import app.packed.component.ComponentDriver;

public abstract class ContainerAssembly<C extends ContainerConfiguration> extends Assembly<C> {

    protected ContainerAssembly(ComponentDriver<? extends C> driver) {
        super(driver);
    }
}
