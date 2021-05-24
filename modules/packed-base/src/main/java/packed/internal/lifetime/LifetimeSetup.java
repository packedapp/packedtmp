package packed.internal.lifetime;

import app.packed.base.Nullable;

// Der er faktisk 2 strategier her...
// RepeatableImage -> Har vi 2 pools taenker jeg... En shared, og en per instans
// Ikke repeatable.. Kav vi lave vi noget af array'et paa forhaand... F.eks. smide
// bean instancerne ind i det

// Saa maaske er pool og Lifetime to forskellige ting???
//
public final class LifetimeSetup {

    // Der er jo som saadan ikke noget vi vejen for at vi har en DAG istedet for et trae...
    @Nullable
    LifetimeSetup parent;

    /** The application's constant pool. */
    public final LifetimePoolSetup pool = new LifetimePoolSetup();

    // Vi kan sagtens folde bedste foraeldre ind ogsaa...
    // Altsaa bruger man kun et enkelt object kan vi jo bare folde det ind...
//    [ [GrandParent][Parent], O1, O2, O3]
}
