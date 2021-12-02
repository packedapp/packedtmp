package app.packed.mirror;

import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.inject.service.ServiceContract;
import app.packed.inject.service.ServiceExtensionMirror;

public class Tester extends BaseAssembly {

    public static void main(String[] args) {

        ContainerMirror.of(new Tester()).useExtension(ServiceExtensionMirror.class).contract();
        ServiceExtensionMirror.of(new Tester()).contract();
        ServiceContract.of(new Tester());
        
        
        // SEM.first(Assembly).
        ContainerMirror cm = ContainerMirror.of(new Tester());

        cm.useExtension(ServiceExtensionMirror.class).contract();

        System.out.println("Exported keys: " + ServiceExtensionMirror.of(new Tester()).contract().provides());

        System.out.println(ServiceContract.of(new Tester()));
        // BaseMirror.reflect(new Tester()).initialization().printAll();

        System.out.println(cm.name());
//        if (ServiceMirror.allExports(cm).anyMatch(s -> s.key() == Key.of(String.class))) {
//            System.out.println("NIcE!!!!!!");
//        }
    }

    @Override
    protected void build() {
        provideInstance("asd").export();

//        // newApplicationHost(Driver);
//        ApplicationHostConfiguration<?> host = ApplicationHostConfiguration.of(configuration(), SomeApp.driver());
//
//        host.lazy(new Tester());
//        host.lazy(new Tester());

    }
}
