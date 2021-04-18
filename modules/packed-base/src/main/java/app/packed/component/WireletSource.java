package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import packed.internal.component.PackedWireletList;

// Hvorfor lige WireletList og ikke f.eks. List eller en annotering
// Fordi der er en masse special metoder... Som kun kan bruges her

// Vi dropper consume foran alt. Da det giver for lange navne.
// Istedet consumer alle metoder paanaer dem der starter med peek

// @DynamicInject

// Okay... Saa kan entent tage 
// ExtensionSetup
// ComponentSetup
// Component
// WireletReceiver
/**
 * A consumable ordered collection of wirelets.
 * 
 * It is consider an error to invoke more than a single method for a single instance. Unless the peek methods. Where you
 * can do what you want
 * 
 * @implNote the current implementation will iterate through every wirelet specified when wiring the component on every
 *           operation. As we expect the number of wirelets for a single component to be small in practice. This is
 *           unlikely to effect performance.
 */

//WireletContainer/WireletHolder/WireletBag

// Do we want to have partial consumes at all??? I cannot think of any usecase.
// When it is not handled

// TODO
// Implement iterator
// Must have matching peek versions of everything (not sure, maybe just a single peek )

// ConsumeAll -> returns whether or not we have any match, useful for boolean is present wirelets
// ConsumeAll -> returns latest
// ConsumeAll -> Fails if more than 1.. (or is this in extension model... probably bad in extensions model.. We need to iterate though all every time then

//WireletSource
public /* sealed */ interface WireletSource<W extends Wirelet> {

    /**
     * Returns every unconsumed wirelet in this source as list, consuming each wirelet in the process.
     * 
     * @return
     */
    default List<W> all() {
        throw new UnsupportedOperationException();
    }

    void forEach(Consumer<? super W> action);

    /** {@return the number of unconsumed wirelets} */

    /**
     * Returns whether or not this handle contains any unconsumed matching wirelets. Consuming each and every matching
     * wirelet.
     * <p>
     * This operation will <strong>not</strong> consume any wirelets.
     * 
     * @return true if at least one matching wirelet, false otherwise
     */
    boolean isEmpty(); // hasMatch

    // forEach
    // will consume any matching wirelet and return the last one..
    // maybe just consume... And fail if there are more than 1 present..
    // consumeAllReturnLast()
    Optional<W> last(); // one() maybe. Emphasize at man consumer en...

    // l.orElse(w->w.launchMode, defaultLaunchmode);
    default <E> E lastOrElse(Function<? super W, ? extends E> mapper, E orElse) {
        requireNonNull(mapper, "mapper is null");
        Optional<W> result = last();
        return result.isEmpty() ? orElse : mapper.apply(result.get());
    }

    default List<W> peekAll() {
        throw new UnsupportedOperationException();
    }

    void peekEach(Consumer<? super W> action);

    /**
     * Returns the number of unconsumed wirelets in this list consuming each wirelet in the process.
     * 
     * @return the number of unconsumed wirelets
     */
    int size();

    /**
     * Returns a wirelet handle with no wirelets to consume
     * 
     * @param <E>
     *            the {@code WireletHandle}'s element type
     * @return an empty wirelet handle
     */
    @SuppressWarnings("unchecked")
    public static <W extends Wirelet> WireletSource<W> of() {
        return (WireletSource<W>) PackedWireletList.EMPTY;
    }

    // Hvad skal vi bruge den her til??? Testing primaert ville jeg mene...
    // Hvad med dem der ikke bliver consumet? skal vi have en WireletHandle.peekCount()???
    @SafeVarargs
    static <W extends Wirelet> WireletSource<W> of(Class<? extends W> wireletClass, Wirelet... wirelets) {
        return PackedWireletList.of(wireletClass, wirelets);
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