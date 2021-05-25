package packed.internal.aaa;

import app.packed.application.App;
import app.packed.container.BaseAssembly;

public class Fooo extends BaseAssembly {

    @Override
    protected void build() {
        provideInstance("asdasd");
        install(Dooo.class);
    }

    public static void main(String[] args) {
        App.run(new Fooo());
        System.out.println("BYe");
    }

    public static class Dooo {
        public Dooo(String s) {
            System.out.println(s);
        }
    }
}
