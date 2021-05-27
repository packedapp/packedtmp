package app.packed.component.instance;

import java.util.Optional;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;
import app.packed.container.ExtensionAncestorRelation;
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

        final int count;

        MyExt(ExtensionContext c) {
            Optional<ExtensionAncestorRelation<MyExt>> ea = c.findParent(MyExt.class);
            if (ea.isPresent()) {
                System.out.println("--- Nice ---");
                System.out.println(ea.get());
                System.out.println(this);
                count = ea.get().instance().count + 1;
            } else {
                count = 0;
            }

            System.out.println("SAD" + c.findParent(Object.class).isPresent());
        }
    }

    class Subbb extends BaseAssembly {

        @Override
        protected void build() {
            System.out.println(use(MyExt.class).count);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            link(new Subbb());
        }
    }
}
