package app.packed.application;

import app.packed.component.Assembly;
import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.Wirelet;
import app.packed.container.ContainerConfiguration;
import app.packed.inject.ServiceConfiguration;

public class ApplicationHostConfiguration<T> extends BaseComponentConfiguration {

    protected ApplicationHostConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    public void lazy(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public void newVersionable() {
        // NICE
    }

    public void provideGenericInstalled() {
        // NICE
    }

    // Hvad er praecis install????
    public void install(Assembly<?> assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    ServiceConfiguration<InstanceManager> managedInstall(Assembly<?> assembly, Wirelet... wirelets) {
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

    // Wirelets are for the host...
    public static <T> ApplicationHostConfiguration<T> of(ContainerConfiguration cc, ApplicationDriver<T> driver, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

interface StaticInstanceManager {}

interface InstanceManager {}

// Vi har lidt droppet tanken om at have klasse vi injecter alle disse klasser i...
// Istedet for er det container wide de ting vi kan lave...