package packed.internal.container;

import java.util.Optional;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.inject.InjectionContext;

public class Eee extends BaseAssembly {

    @Override
    protected void build() {
        use(DDD.class);

    }

    public static void main(String[] args) {
        App.run(new Eee());
    }

    static class DDD extends Extension {
        DDD(InjectionContext ic, Optional<DDD> parent) {
            System.out.println(ic.keys());
        }
    }
}
