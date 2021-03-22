package app.packed.component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import packed.internal.component.wirelet.PackedWireletHandle;

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
 * @implNote the current implementation will iterate through every wirelet specified when wiring the component on every
 *           operation. As we expect the number of wirelets for a single component to be small in practice. This is
 *           unlikely to effect performance.
 */
//WireletContainer/WireletHolder/WireletBag
public /* sealed */ interface WireletHandle<W extends Wirelet> {

    default List<W> consumeAll() {
        throw new UnsupportedOperationException();
    }

    void consumeEach(Consumer<? super W> action);

    /** {@return the number of unconsumed wirelets} */
    int count();

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

    boolean isEmpty();

    // forEach
    // will consume any matching wirelet and return the last one..
    // maybe just consume... And fail if there are more than 1 present..
    Optional<W> last(); // one() maybe. Emphasize at man consumer en...

    default List<W> peekAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns a wirelet handle with no wirelets to consume
     * 
     * @param <E>
     *            the {@code WireletHandle}'s element type
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