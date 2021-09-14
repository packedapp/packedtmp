package app.packed.mirror;

import app.packed.application.ApplicationMirror;
import app.packed.application.host.ApplicationHostConfiguration;
import app.packed.application.programs.SomeApp;
import app.packed.bundle.BaseBundle;
import app.packed.bundle.BundleMirror;
import app.packed.service.ServiceContract;
import app.packed.service.ServiceExtensionMirror;

public class Tester extends BaseBundle {

    public static void main(String[] args) {

        for (ApplicationMirror am : ApplicationMirror.of(new Tester()).findAllinstallations(false)) {
            System.out.println(am);
        }

        BundleMirror.of(new Tester()).useExtension(ServiceExtensionMirror.class).contract();
        ServiceExtensionMirror.of(new Tester()).contract();
        ServiceContract.of(new Tester());
        
        
        // SEM.first(Assembly).
        BundleMirror cm = BundleMirror.of(new Tester());

        cm.useExtension(ServiceExtensionMirror.class).contract();

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
        ApplicationHostConfiguration<?> host = ApplicationHostConfiguration.of(configuration(), SomeApp.driver());

        host.lazy(new Tester());
        host.lazy(new Tester());

    }
}
