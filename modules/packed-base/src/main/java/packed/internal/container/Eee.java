package packed.internal.container;

import app.packed.application.App;
import app.packed.component.ComponentMirror;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionConfiguration;
import app.packed.inject.InjectionContext;

public class Eee extends BaseAssembly {

    @Override
    protected void build() {
        use(DDD.class);
        ComponentMirror m = link(new MyL());
        m.stream().forEach(e -> System.out.println(e.path()));
    }

    public static void main(String[] args) {
        App.run(new Eee());
    }

    static class DDD extends Extension {
        DDD(ExtensionConfiguration ec, InjectionContext ic /* , Optional<DDD> parent */) {
            System.out.println(ic.keys());

        }
    }

    static class MyL extends BaseAssembly {

        @Override
        protected void build() {
            installInstance("asdads");
            new Exception().printStackTrace();
        }

    }
}
