package app.packed.application.host;

import app.packed.component.Assembly;
import app.packed.component.ComponentDriver;

public abstract class ApplicationHostAssembly<T> extends Assembly<ApplicationHostConfiguration<T>> {

    protected ApplicationHostAssembly(ComponentDriver<? extends ApplicationHostConfiguration<T>> driver) {
        super(driver);
    }

}
