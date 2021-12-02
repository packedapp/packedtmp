package app.packed.hooks.sandbox;

import app.packed.application.programs.SomeApp;
import app.packed.base.Nullable;
import app.packed.container.BaseAssembly;
import app.packed.extension.Extension;
import app.packed.inject.service.ServiceExtension;

public class ChildInject extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class).foo();
        link(new Fff());
    }

    public static void main(String[] args) {
        SomeApp.run(new ChildInject());
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

    public static class MyExt extends Extension {

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

        static {
            $dependsOn(ServiceExtension.class);
        }
    }
}
