package app.packed.zzzz;

import app.packed.application.App;
import app.packed.application.AsyncApp;
import app.packed.build.BuildWirelets;
import app.packed.container.BaseAssembly;
import app.packed.container.Wirelet;
import app.packed.lifecycle.RunState;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        named("asdasd");
        link(new LinkMe(), Wirelet.named("heher"));
    }

    public static void main(String[] args) {
        App.run(new ZapAssembly(), BuildWirelets.spyOnWire(c -> System.out.println(c.path())));

        AsyncApp.mirrorOf(new ZapAssembly());

        // Det gode ved mirror er at
        // Daemon.introspect(new ZapAssembly());
        // Daemon.reflect(new ZapAssembly());
        AsyncApp.mirrorOf(new ZapAssembly());

        App.driver().print(new ZapAssembly());
    }

    public static class LinkMe extends BaseAssembly {

        @Override
        protected void build() {
            installInstance("SDADs");
            install(My.class);

            install(My.class).on(RunState.RUNNING, e -> System.out.println(e + "OK"));
            installInstance("adasd");
            installInstance("asdasd");

            bean().install(My.class);
            install(My.class);
        }

    }

    public static class My {}
}
