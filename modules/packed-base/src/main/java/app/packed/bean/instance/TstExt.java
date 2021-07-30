package app.packed.bean.instance;

import java.util.Optional;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionConfiguration;
import app.packed.extension.old.ExtensionBeanConnection;

public class TstExt extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class);
        installInstance("werwer").inject("xxxx");
        link(new Subbb());
    }

    public static void main(String[] args) {
        App.run(new TstExt());
    }

    static class MyExt extends Extension {

        final int count;

        MyExt(ExtensionConfiguration c) {
            Optional<ExtensionBeanConnection<MyExt>> ea = c.findParent(MyExt.class);
            if (ea.isPresent()) {
                System.out.println("--- Nice ---");
                System.out.println(ea.get());
                System.out.println(this);
                count = ea.get().instance().count + 1;
            } else {
                count = 0;
            }

            System.out.println("SAD" + c.findParent(MyExt.class).isPresent());
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