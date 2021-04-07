package packed.internal.application;

import app.packed.application.BuildWirelets;
import app.packed.application.Program;
import app.packed.container.BaseAssembly;

public class WTest extends BaseAssembly {

    @Override
    protected void build() {
        installInstance("FOO");
        provideInstance("FOO").export();
    }

    public static void main(String[] args) {
        Program p = Program.start(new WTest(), BuildWirelets.onWire(c -> System.out.println("Wired " + c.path())));

        System.out.println(p.services());

        System.out.println(p.name());
    }

}
