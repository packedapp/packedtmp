package app.packed.container;

import java.util.Optional;

import app.packed.cli.Main;

public class ChildInject extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class);
        link(new Fff());
    }

    public static void main(String[] args) {
        Main.run(new ChildInject());
    }

    public static class MyExt extends Extension {

        MyExt(Optional<MyExt> ancestor) {
            System.out.println("NICE " + ancestor);
        }
        
        @ConnectExtensions
        public void ff(MyExt me) {
            System.out.println("Linked " + me);
        }
    }

    static class Fff extends BaseAssembly {

        @Override
        protected void build() {
            use(MyExt.class);
        }
    }
}
