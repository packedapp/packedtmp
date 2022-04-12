package app.packed.bean.oldhooks.sandbox;

import app.packed.application.App;
import app.packed.base.Nullable;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.extension.Extension.DependsOn;
import app.packed.inject.service.ServiceExtension;

public class ChildInject extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class).foo();
        link(new Fff());
    }

    public static void main(String[] args) {
        App.run(new ChildInject());
    }

    static class Fff extends BaseAssembly {

        static int i = 4;

        @Override
        protected void build() {
            use(MyExt.class);
            if (i-- > 0) {
                System.out.println("Linking " + link(new Fff()).path());
            }
        }
    }

    @DependsOn(extensions = ServiceExtension.class)
    public static class MyExt extends Extension<MyExt> {

        final String name;
        final long l = System.nanoTime();

        MyExt(@Nullable MyExt parent) {
            if (parent == null) {
                name = "Root";
            } else {
                name = (parent.name == null ? "" : parent.name) + "Child";
            }
            System.out.println("Creating " + name);
        }

        public String toString() {
            return name;
        }

        public void foo() {
            use(ServiceExtension.ServiceExtensionSupport.class).check();
        }
    }
}
