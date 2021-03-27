package packed.internal.application;

import app.packed.application.ApplicationImage;
import app.packed.application.BuildWirelets;
import app.packed.application.Program;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;

public class WTest extends BaseAssembly {

    @Override
    protected void build() {
        provideInstance("FOO").export();
    }

    public static void main(String[] args) {
        Program p = Program.start(new WTest(), Wirelet.named("sdfsf"), BuildWirelets.onWire(c -> System.out.println("Wired " + c.path())));
        
        System.out.println(p.services());
        
        System.out.println(p.name());
        
        
        Program.driver().analyze(new WTest()).resolve(".ServiceExtension").attributes().print();

        ApplicationImage<Program> pp = Program.newImage(new WTest(), Wirelet.named("sdfsf"));
        p = pp.apply(Wirelet.named("XXX 1"), Wirelet.named("XXX 34"));

        System.out.println(p.name());
    }

}
