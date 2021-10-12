package packed.internal.component;

import app.packed.application.Daemon;
import app.packed.application.programs.SomeApp;
import app.packed.build.BuildWirelets;
import app.packed.bundle.BaseAssembly;
import app.packed.bundle.Wirelet;
import app.packed.state.sandbox.InstanceState;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        named("asdasd");
        link(new LinkMe(), Wirelet.named("heher"));
    }

    public static void main(String[] args) {
        SomeApp.run(new ZapAssembly(), BuildWirelets.spyOnWire(c -> System.out.println(c.path())));

        Daemon.mirrorOf(new ZapAssembly());

        // Det gode ved mirror er at
        //Daemon.introspect(new ZapAssembly());
        //Daemon.reflect(new ZapAssembly());
        Daemon.mirrorOf(new ZapAssembly());

        SomeApp.driver().print(new ZapAssembly());
    }

    static class LinkMe extends BaseAssembly {

        @Override
        protected void build() {
            installInstance("SDADs");
            install(My.class);

            install(My.class).on(InstanceState.RUNNING, e -> System.out.println(e + "OK"));
            installInstance("adasd");
            installInstance("asdasd");

            bean().install(My.class);
            install(My.class);
        }

    }

    static class My {}
}
