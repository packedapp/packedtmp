package app.packed.application.host;

import java.util.function.Supplier;

import app.packed.application.ApplicationDriver;
import app.packed.application.ApplicationImage;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.Wirelet;
import app.packed.service.ServiceConfiguration;
import packed.internal.util.NativeImage;

/**
 * The configuration of an application host.
 */
// Foo App:/asd/asdasd  Fpp/String:/qwe/sds (Ved ikke hvor meget det giver mening at forfoelge
// componenter i forskellige applicationer som standard...
// Det er lidt efter vi maaske tilfoejer en installer som en component child til en host
// Saa ligger den paa niveau med alle hostede applikationer...
// Omvendt er det jo rart at kunne iterere over alle containere og fange alle exceptions...

//Kan vi have en Host med forskellige Applications typer for en host????
//Jeg har svaert ved at se det... Saa maa man lave forskellige hosts...
//I 9/10 af tilfaeldene er de vel ogsaa void...

// Har vi en AbstractApplicationHostConfiguration???
public class ApplicationHostConfiguration<T> {

    public InstalledApplicationConfiguration<T> install(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public ServiceConfiguration<ApplicationImage<T>> installLaunchable(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Can fail, what if never started
    // Maybe CompletaableFuture istedet for...
    public Supplier<T> lazy(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    ServiceConfiguration<InstanceManager> managedInstall(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public ServiceConfiguration<ApplicationImage<T>> multiLauncher(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Hmm hvad hvis vi vil bootstrappe den...
    // Biver nok noedt til at have en type inden...
    ServiceConfiguration<VersionableApplicationHost> newVersionable() {
        // NICE
        throw new UnsupportedOperationException();
    }

    public void provideGenericInstalled() {
        // NICE
    }

    ServiceConfiguration<InstalledApplicationConfiguration<T>> provideInstaller() {
        if (NativeImage.inImageBuildtimeCode()) {
            throw new UnsupportedOperationException("Application installers are not supported for native images");
        }
        // I think we can only provide one installer???
        // Maaske vi ikke en engang faar en ServiceConfiguration tilbage...
        // Der er bare en ServiceInstaller tilraadighed
        // Vi installere den som et component til hosten....

        // Hvor installere vi en optional service...
        // provide(Key<?> key, MethodHandle) // MethodHandle paa instancen...

        // Okay vi bliver jo noedt til at have en eller anden klasse...
        // PackedApplicationHost

        // Is not supported on GraalVM native image..
        throw new UnsupportedOperationException();
    }

    ServiceConfiguration<StaticInstanceManager> provideManager() {
        // Ideen er lidt at vi provider forskellige services.. Som andre
        // componenter i containeren kan bruge

        // Den her metode er egentlig taenkt paa iterere over ALLE instancer...
        // Derfor kan vi naturligvis ikke instantiere fra den...
        // Da vi har flere assemblies...
        // Den her metode er paa ALLE

        throw new UnsupportedOperationException();
    }

    // A new application that can be launched exactly once... It is a failure to
    public ServiceConfiguration<ApplicationImage<T>> singleLauncher(Bundle<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    // Hvis man har en context kan man goere hvad man vil
    @SuppressWarnings("unused")
    private static <T> ApplicationHostConfiguration<T> of(/* ComponentConfigurationContext context, */ ApplicationDriver<T> driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

//    // Det er jo en salgs binder...
//    public static <T> ComponentDriver<ApplicationHostConfiguration<T>> newDriver(ApplicationDriver<T> driver, Wirelet... wirelets) {
//        throw new UnsupportedOperationException();
//    }

    // Tror vi maa flytten den til ComponentConfiguration...
    public static <T> ApplicationHostConfiguration<T> of(BundleConfiguration cc, ApplicationDriver<T> driver, Wirelet... wirelets) {
        // Den er sjov...
        // Vi har ikke rigtig en klasse...
        // Men en holder, og saa er alle de ting hvor hvor vi propper (service configuration) managedInstall blot services

        // Maaske kan man mappe componenten til et mirror?

       // ComponentDriver<ApplicationHostConfiguration<T>> driv = null;
        //return cc.wire(driv, wirelets);
        throw new UnsupportedOperationException(); 
    }
}

interface InstanceManager {}

class OldStuff<T> extends ApplicationHostConfiguration<T> {

    // Det er her hvor jeg maaske taenker vi skal have noget wirelet praeprocessering...
    // Vi vil gerne have launchmode i mirrors...
    // Men det kan vi ikke naar det bare er en wirelet..
    // onBuild(), onInitialize()
    public void lazy2(Bundle<?> assembly, Wirelet... wirelets) {
      //  install(assembly, ExecutionWirelets.launchMode(ApplicationLaunchMode.RUNNING).beforeThis(wirelets));
    }
}

interface StaticInstanceManager {}

interface VersionableApplicationHost {}

// Vi har lidt droppet tanken om at have klasse vi injecter alle disse klasser i...
// Istedet for er det container wide de ting vi kan lave...