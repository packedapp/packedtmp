package app.packed.component;

import java.util.Optional;
import java.util.function.Consumer;

import packed.internal.component.wirelet.WireletPack;

// Det gode ved den her er at den jo samtidig fungere som
// en optional faetter
// WireletContainer/WireletHolder/WireletBag
// @DynamicInject

// Okay... Saa kan entent tage 
// ExtensionSetup
// ComponentSetup
// Component
// WireletReceiver
/**
 * It is consider an error to invoke more than a single method for a single instance. Unless the peek methods. Where you
 * can do what you want
 * 
 * @implNote the current implementation will iterate through every single wirelet of a component for every operation. As
 *           we expect the number of wirelets for a single component to be small in practice. This is unlikely to effect
 *           performance.
 */
public /* sealed */ interface WireletHandle<T extends Wirelet> {

    void forEach(Consumer<? super T> action);

    /**
     * Returns whether or not this handle contains any unconsumed matching wirelets. Consuming each and every matching
     * wirelet.
     * <p>
     * 
     * @return true if at least one matching wirelet, false otherwise
     * @throws IllegalStateException
     *             if this handler has been handled
     */
    boolean isAbsent(); // hasMatch

    boolean isPresent();
    
    // forEach
    // will consume any matching wirelet and return the last one...
    Optional<T> last(); // one() maybe. Emphasize at man consumer en...

    @SafeVarargs
    static <T extends Wirelet> WireletHandle<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        return WireletPack.handleOf(wireletClass, wirelets);
    }
}
//Collect, Receive, Accept, Consume

//Grunden til jeg ikke kan lide WireletInject er den kan puttes paa en parameter...
//Men det kan @Inject ikke.
//@Nullable, Optional, List
//VarHandle, MethodHandle doesn't really work with WireletHandle...
//methods are conditional invoked.....

//Hmm Hvad hvis jeg har foo(Optional<EEE>)

//Skal den virkelig invokes alligevel???
//
//@WireletLink...  Nah @WireletLink Optional<>

//Den her doede fordi vi ikke kan lide UseWirelet..