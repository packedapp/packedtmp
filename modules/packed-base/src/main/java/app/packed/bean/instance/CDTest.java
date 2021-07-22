package app.packed.bean.instance;

import app.packed.component.Wirelet;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.container.ContainerWirelets;
import app.packed.service.ServiceExtension;

public class CDTest {

    public static abstract class MyAss extends Assembly<ContainerConfiguration> {

        static final ContainerDriver<ContainerConfiguration> D = ContainerDriver.of(() -> new ContainerConfiguration() {},
                ContainerWirelets.disableExtension(ServiceExtension.class));

        protected MyAss(Wirelet... wirelets) {
            super(D.with(wirelets));
        }
    }
}
