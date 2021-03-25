package packed.internal.component.source;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.inject.InjectionContext;

public class Ffff extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class);
    }

    public static void main(String[] args) {
        App.run(new Ffff());
    }

    public static class MyExt extends Extension {
        MyExt() {}
        MyExt(InjectionContext ic) {}
    }
}
