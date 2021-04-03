package packed.internal.inject.dependency.sandbox;

import java.util.Optional;

import app.packed.application.App;
import app.packed.container.BaseAssembly;
import app.packed.container.Extension;

public class Ffff extends BaseAssembly {

    @Override
    protected void build() {
        use(MyExt.class);
    }

    public static void main(String[] args) {
        App.run(new Ffff());
    }

    public static class MyExt extends Extension {
        MyExt(Optional<String> op) {
            System.out.println(op);
        }
    }

}
