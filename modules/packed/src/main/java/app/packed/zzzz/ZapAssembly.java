package app.packed.zzzz;

import app.packed.application.App;
import app.packed.application.ApplicationMirror;
import app.packed.base.Tag;
import app.packed.container.BaseAssembly;
import app.packed.container.BuildWirelets;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.ExtensionPoint;
import app.packed.container.Wirelet;
import app.packed.container.Extension.DependsOn;
import app.packed.inject.Factory1;
import app.packed.inject.InjectionContext;
import packed.internal.devtools.spi.PackedDevTools;

public class ZapAssembly extends BaseAssembly {

    @Override
    protected void build() {
        PackedDevTools.INSTANCE.goo();

        new Exception().printStackTrace();

        // Det kan man ikke pga af lifecycle annotations
        // beanExtension.newAlias(ContainerBean.class).exportAs()
        named("asdasd");
        
        use(MyExt2.class);

        install(new Factory1<String, @Tag("asd") String>(e -> e) {});

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

            // install(My.class).on(RunState.RUNNING, e -> System.out.println(e + "OK"));
            installInstance("adasd");
            installInstance("asdasd");

            bean().install(My.class);
            install(My.class);

        }

    }

    public static class My {}

    public static class MyExt extends Extension<MyExt> {
        void nice() {
            System.out.println("NICEECEE");
        }
    }

    @DependsOn(extensions = MyExt.class)
    public static class MyExt2 extends Extension<MyExt2> {
        protected void onNew() {
            use(MyExptPoint.class);
        }
    }

    public static class MyExptPoint extends ExtensionPoint<MyExt> {
        MyExptPoint(UseSite c, InjectionContext ic) {
            System.out.println("XXXX"  + c.extensionType());
            System.out.println(ic.keys());
        }
        
    }
}
