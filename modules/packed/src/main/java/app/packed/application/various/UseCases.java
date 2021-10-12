package app.packed.application.various;

import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationLaunchMode;
import app.packed.application.ApplicationRuntime;
import app.packed.application.Daemon;
import app.packed.application.programs.SomeApp;
import app.packed.base.Completion;
import app.packed.bundle.BaseAssembly;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.host.ApplicationHostConfiguration;
import app.packed.bundle.host.ApplicationHostExtension;
import app.packed.lifecycle.OnStart;
import app.packed.state.sandbox.InstanceState;

public class UseCases {

    public void lazy(BundleConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(cc, SomeApp.driver().withLaunchMode(ApplicationLaunchMode.RUNNING));
        hc.install(new AA()/* , WebWirelets.setRoot("foo") */ );
        hc.install(new BB());
    }

    public void lazy2(BundleConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(cc,
                SomeApp.driver().withLaunchMode(ApplicationLaunchMode.RUNNING));
        hc.install(new AA()/* , WebWirelets.setRoot("foo") */ );
        hc.install(new BB());
    }

    public void singleInstanceAcquiring(BundleConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(null, SomeApp.driver().withLaunchMode(ApplicationLaunchMode.RUNNING));

        hc.install(new AA()).provideSingleLauncher();
        hc.installLaunchable(new AA());
        // cc.install(AppLauncher.class);
    }

    public void singleInstanceWithGuest(BundleConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(null, SomeApp.driver().withLaunchMode(ApplicationLaunchMode.RUNNING));

        hc.install(new AA()).provideGuest();
        hc.installLaunchable(new AA());
        // cc.install(AppLauncher.class);
    }

    public interface Guest {
        InstanceState state();

        ApplicationRuntime runtime();
        // await
        // und so weither
    }

}

class HostAsExtension extends BaseAssembly {

    @Override
    protected void build() {
        use(ApplicationHostExtension.class).delayedInitialization(new BB());
    }

}

class AppLauncher {

    @OnStart
    public void foo(ApplicationImage<Daemon> l) throws InterruptedException {
        Daemon d = l.use();
        Thread.sleep(10000);
        d.stop();
    }
}

class AA extends BaseAssembly {

    @Override
    protected void build() {}
}

class BB extends BaseAssembly {

    @Override
    protected void build() {}
}
