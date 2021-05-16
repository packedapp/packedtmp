package app.packed.mirror;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.application.host.ApplicationHostConfiguration;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtensionMirror;

public class Tester extends BaseAssembly {

    public static void main(String[] args) {

        for (ApplicationMirror am : ApplicationMirror.of(new Tester()).findAllinstallations(false)) {
            System.out.println(am);
        }

        ContainerMirror.of(new Tester()).use(ServiceExtensionMirror.class).contract();
        ServiceExtensionMirror.of(new Tester()).contract();
        
        
        // SEM.first(Assembly).
        ContainerMirror cm = ContainerMirror.of(new Tester());

        cm.use(ServiceExtensionMirror.class).contract();

        System.out.println("Exported keys: " + ServiceExtensionMirror.of(new Tester()).exportedKeys());

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

        // newApplicationHost(Driver);
        ApplicationHostConfiguration<?> host = ApplicationHostConfiguration.of(configuration(), App.driver());

        host.lazy(new Tester());
        host.lazy(new Tester());

    }
}
