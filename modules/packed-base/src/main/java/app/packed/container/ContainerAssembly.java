package app.packed.container;

import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;

public abstract class ContainerAssembly extends Assembly<ComponentConfiguration> {

    protected ContainerAssembly(ComponentDriver<? extends ComponentConfiguration> driver) {
        super(driver);
    }
}
