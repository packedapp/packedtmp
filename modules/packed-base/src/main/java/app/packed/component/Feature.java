package app.packed.component;

import app.packed.config.ConfigSite;
import app.packed.container.Extension;

//Saa det er udelukkende styret af sidecars tilknyttet en component....
//Understoetter ogsaa build-time vs runtime sidecars....
// Altsaa DependencyGraphFeature har vi jo indtil en container er initializeret...
// Saa maaske kan de sidecar transformere sig som de har lyst...

// Maaske man endda kan have noget retain(DependencyGraphFeature.class)

// Features er pretty static...

// Problemet er runtime registrering af f.eks. job tasks....

//Lokale services... for componenter...

// Skal man tage en ComponentContext med?
// Eller skal man ComponentContext.use(Scheduler.class).installl

// Feature er maaske lidt daarlig pga Feature flags...
public interface Feature {

    /**
     * Returns the configuration site of the feature.
     * 
     * @return the configuration site of the feature
     */
    ConfigSite configSite();

    /**
     * Returns the extension to which this feature belongs.
     * 
     * @return the extension to which this feature belongs
     */
    Class<? extends Extension> extension();
}
// Det er jo egentlig et slags MultiMap....

//Map<ComponentPath, MultiMap<Class<? extends Feature>, Feature>>

// Is a Contract a kind of feature????

interface SingleFeature extends Feature {}

interface MultiFeature extends Feature {}

//Men man kan jo stadig ikke sige..
// Class<T extends SingleFeature> T get(Class<T> t);
// Class<T extends MultiFeature> List<T> getAll(Class<T> t);

// @FeatureProvider
// Optional<XFeature>, @Nullable XFeature, XFeature, List/Collection/Set <XFeature>