package app.packed.container;

import app.packed.cli.Main;
import app.packed.inject.ServiceExtension;

public class ChildInject extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class).foo();
        link(new Fff());
    }

    public static void main(String[] args) {
        Main.run(new ChildInject());
    }

    static class Fff extends BaseAssembly {

        @Override
        protected void build() {
            use(MyExt.class);
        }
    }

    public static class MyExt extends Extension {

        final long l = System.nanoTime();

        MyExt(MyExt me) {
            if (me != null) {
                System.out.println("NICE parent[" + me.l + "], me =" + l);
            }
        }

        @ConnectExtensions
        public void ff(MyExt me) {
            System.out.println("Linked me=" + l + " other = " + me.l);
        }

        public void foo() {
            use(ServiceExtension.Sub.class).check();
        }

        static {
            $dependsOn(ServiceExtension.class);
        }
    }
}
