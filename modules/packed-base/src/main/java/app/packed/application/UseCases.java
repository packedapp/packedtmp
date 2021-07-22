package app.packed.application;

import app.packed.application.host.ApplicationHostConfiguration;
import app.packed.application.host.ApplicationHostExtension;
import app.packed.base.Completion;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.lifecycle.OnStart;
import app.packed.state.sandbox.InstanceState;

public class UseCases {

    public void lazy(ContainerConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(cc, App.driver().withLaunchMode(InstanceState.UNINITIALIZED));
        hc.install(new AA()/* , WebWirelets.setRoot("foo") */ );
        hc.install(new BB());
    }

    public void lazy2(ContainerConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(cc,
                App.driver().with(ApplicationRuntimeWirelets.launchMode(InstanceState.UNINITIALIZED)));
        hc.install(new AA()/* , WebWirelets.setRoot("foo") */ );
        hc.install(new BB());
    }

    public void singleInstanceAcquiring(ContainerConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(null, App.driver().withLaunchMode(InstanceState.UNINITIALIZED));

        hc.install(new AA()).provideSingleLauncher();
        hc.installLaunchable(new AA());
        // cc.install(AppLauncher.class);
    }

    public void singleInstanceWithGuest(ContainerConfiguration cc) {
        ApplicationHostConfiguration<Completion> hc = ApplicationHostConfiguration.of(null, App.driver().withLaunchMode(InstanceState.UNINITIALIZED));

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
    public void foo(Launcher<Daemon> l) throws InterruptedException {
        Daemon d = l.launch();
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
