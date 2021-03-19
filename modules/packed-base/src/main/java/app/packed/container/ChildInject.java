package app.packed.container;

import java.util.Optional;

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

        MyExt(Optional<MyExt> ancestor) {
            System.out.println("NICE " + ancestor);
        }

        @ConnectExtensions
        public void ff(MyExt me) {
            System.out.println("Linked " + me);
        }

        public void foo() {
            use(ServiceExtension.Sub.class).check();
        }

        static {
            $dependsOn(ServiceExtension.class);
        }
    }
}
