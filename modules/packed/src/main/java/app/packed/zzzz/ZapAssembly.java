package app.packed.zzzz;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.build.BuildWirelets;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;
import app.packed.container.Wirelet;
import app.packed.extension.Extension;
import app.packed.lifecycle.RunState;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        new Exception().printStackTrace();

        named("asdasd");
        link(new LinkMe(), Wirelet.named("heher"));
    }

    public static void main(String[] args) {
        App.run(new ZapAssembly(), BuildWirelets.spyOnWire(c -> System.out.println(c.path())));
        ApplicationMirror am = App.mirrorOf(new ZapAssembly());

        System.out.println(am.container().extensionTypes());
        am.container().children().forEach(c -> {
            System.out.println(c.path());
            System.out.println(((ContainerMirror) c).extensionTypes());
        });
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
    
    public static class MyExt extends Extension {
        
    }
}
