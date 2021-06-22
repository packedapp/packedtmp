package app.packed.component.instance;

import app.packed.component.Wirelet;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerDriver;
import app.packed.container.ContainerWirelets;
import app.packed.service.ServiceExtension;

public class CDTest {

    public static void main(String[] args) {
//        ContainerDriver<BaseContainerConfiguration> cd = ;
    }

    public static abstract class MyAss extends ContainerAssembly<ContainerConfiguration> {

        static final ContainerDriver<ContainerConfiguration> D = ContainerDriver.of(() -> new ContainerConfiguration() {},
                ContainerWirelets.disableExtension(ServiceExtension.class));

        protected MyAss(Wirelet... wirelets) {
            super(D.with(wirelets));
        }
    }
}
