package app.packed.mirror;

import app.packed.application.App;
import app.packed.base.Key;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;

public class Tester extends BaseAssembly {

    public static void main(String[] args) {
        ContainerMirror cm = App.driver().mirror(new Tester()).container();
        System.out.println(cm.name());
        if (ServiceMirror.allExports(cm).anyMatch(s -> s.key() == Key.of(String.class))) {
            System.out.println("NIcE!!!!!!");
        }
    }

    @Override
    protected void build() {
        // TODO Auto-generated method stub
    }
}
