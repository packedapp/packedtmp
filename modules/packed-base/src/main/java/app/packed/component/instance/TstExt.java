package app.packed.component.instance;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionAncestor;
import app.packed.container.ExtensionContext;

public class TstExt extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class);
        link(new Subbb());
    }

    public static void main(String[] args) {
        App.run(new TstExt());
    }

    static class MyExt extends Extension {

        MyExt(ExtensionContext c) {
            ExtensionAncestor<Object> ea = c.findAncestor(Object.class);
            if (ea.isPresent()) {
                System.out.println("--- Nice ---");
                System.out.println(ea.get());
                System.out.println(this);
            }
            System.out.println("SAD" + c.findAncestor(Object.class).isPresent());

        }
    }

    class Subbb extends BaseAssembly {

        @Override
        protected void build() {
            use(MyExt.class);
        }
    }
}
