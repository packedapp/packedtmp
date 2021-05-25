package packed.internal.aaa;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionContext;
import app.packed.inject.InjectionContext;
import app.packed.inject.ServiceExtension;
import app.packed.inject.ServiceExtensionMirror;

public class Eee extends BaseAssembly {

    @Override
    protected void build() {
        use(DDD.class);
        link(new MyL());

        // System.out.println(m);
//        for (ExtensionMirror<?> em : m.container().extensions()) {
//            System.out.println(em.type() + ": dependencies = " + em.descriptor().dependencies());
//            System.out.println(em.getClass());
//        }
//        System.out.println("Extensions: " + m.container().extensions());

//        m.stream().forEach(e -> System.out.println(e.path()));
    }

    public static void main(String[] args) {
        App.run(new Eee());
        System.out.println();
        System.out.println("---------");
        App.driver().print(new Eee());

        ApplicationMirror m = ApplicationMirror.of(new MyL());
        
        System.out.println(m.useExtension(ServiceExtensionMirror.class).contract());
        
//        for (ExtensionMirror<?> mm : m.application().extensions()) {
//            System.out.println("----------- " + mm.getClass());
//        }
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
            export(String.class);
            // new Exception().printStackTrace();
        }
    }

    static class MyExte extends Extension {

        static {
            $dependsOn(ServiceExtension.class);
        }
    }

}
