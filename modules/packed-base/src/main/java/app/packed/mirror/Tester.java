package app.packed.mirror;

import app.packed.application.App;
import app.packed.application.BaseMirror;
import app.packed.base.Key;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.inject.ServiceContract;
import app.packed.inject.ServiceExtensionMirror;

public class Tester extends BaseAssembly {

    public static void main(String[] args) {
        ContainerMirror cm = App.driver().mirror(new Tester()).container();
        
        System.out.println("Exported keys: " + ServiceExtensionMirror.reflect(new Tester()).exportedKeys());
        
        System.out.println(ServiceContract.of(new Tester()));
        BaseMirror.reflect(new Tester()).initialization().printAll();
        
        

        System.out.println(cm.name());
        if (ServiceMirror.allExports(cm).anyMatch(s -> s.key() == Key.of(String.class))) {
            System.out.println("NIcE!!!!!!");
        }
    }

    @Override
    protected void build() {
        provideInstance("asd").export();
        // TODO Auto-generated method stub
    }
}
