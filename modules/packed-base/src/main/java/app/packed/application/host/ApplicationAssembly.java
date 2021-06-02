package app.packed.application.host;

import app.packed.component.Assembly;
import app.packed.component.ComponentConfiguration;
import app.packed.component.ComponentDriver;

public abstract class ApplicationAssembly<C extends ComponentConfiguration> extends Assembly<C> {

    protected ApplicationAssembly(ComponentDriver<? extends C> driver) {
        super(driver);
    }

}
