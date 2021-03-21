package app.packed.component;

import java.util.Optional;
import java.util.function.Consumer;

import packed.internal.component.wirelet.PackedWireletHandle;

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
public /* sealed */ interface WireletHandle<W extends Wirelet> {

    /**
     * Returns the number of uncosumed ... consuming the wirelets in the process.
     * 
     * If you do not wish to consume the wirelet call {@link #peekCount()}.
     * 
     * @return asdasd
     * 
     * @see #peekCount()
     */
    default int count() {
        return 0;
    }

    void forEach(Consumer<? super W> action);

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
    Optional<W> last(); // one() maybe. Emphasize at man consumer en...

    /**
     * Unlike {@link #count()} this method does not consume any wirelets.
     * 
     * @return the number of matching wirelets that have not yet been consumed
     * @see #count()
     */
    default int peekCount() {
        return 0;
    }

    /**
     * Returns a wirelet handle with no wirelets to consume
     * 
     * @param <E> the {@code WireletHandle}'s element type
     * @return an empty wirelet handle
     */
    public static <W extends Wirelet> WireletHandle<W> of() {
        return PackedWireletHandle.of();
    }

    // Hvad skal vi bruge den her til??? Testing primaert ville jeg mene...
    // Hvad med dem der ikke bliver consumet? skal vi have en WireletHandle.peekCount()???
    @SafeVarargs
    static <W extends Wirelet> WireletHandle<W> of(Class<? extends W> wireletClass, Wirelet... wirelets) {
        return PackedWireletHandle.of(wireletClass, wirelets);
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