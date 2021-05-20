package packed.internal.container;

import app.packed.application.App;
import app.packed.component.ComponentMirror;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.container.ExtensionMirror;
import app.packed.inject.InjectionContext;
import app.packed.inject.ServiceExtension;

public class Eee extends BaseAssembly {

    @Override
    protected void build() {
        use(DDD.class);
        ComponentMirror m = link(new MyL());
        
        
        for (ExtensionMirror<?> em : m.container().extensions()) {
            System.out.println(em.type() + ": dependencies = " + em.descriptor().dependencies());
            System.out.println(em.getClass());
        }
        System.out.println("Extensions: " + m.container().extensions());
        
        m.stream().forEach(e -> System.out.println(e.path()));
    }

    public static void main(String[] args) {
        App.run(new Eee());
    }

    static class DDD extends Extension {
        DDD(ExtensionContext ec, InjectionContext ic /* , Optional<DDD> parent */) {
            System.out.println(ic.keys());

        }
    }

    static class MyL extends BaseAssembly {

        @Override
        protected void build() {
            installInstance("asdads");
            service();
            use(MyExte.class);
            // new Exception().printStackTrace();
        }
    }

    static class MyExte extends Extension {

        static {
            $dependsOn(ServiceExtension.class);
        }
    }

}
