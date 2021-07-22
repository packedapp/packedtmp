package app.packed.component;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import packed.internal.component.PackedWireletSelection;

/**
 * A selection of wirelets of a specified type represented by {@code <W>} .
 * <p>
 * If there are multiple wirelets in the selection, they will ordered accordingly to the following rules:
 * <ul>
 * <li><b>driver</b> fooo</li>
 * <li><b>Build-time / wire-time</b></li>
 * <li><b>component driver</b></li>
 * <li><b>launch</b>Finally, wirelets specified at application launch time</li>
 * </ul>
 * <p>
 * Skal vi have wildcard? SelectWirelets<? extends SSSfoo> eller supportere vi ogsaa SelectWirelets<SSSfoo>, jeg ville
 * mene vi supportere begge.
 * 
 * Maaske selecter vi differently??? ... exact type for SelectWirelet<FooWirelet>, eller alle super klasser for
 * SelectWirelet<? extends FooWirelet>
 * 
 * <p>
 * The selecting class must be in the same module as the type of wirelet selected
 * <p>
 * There a couple of ways to select wirelets. For extension space wirelets
 * 
 * at buildtime:. ExtensionConfiguration#selectWirelets, Extension#selectWirelets Hook injection?? Could make sense for
 * some class hooks
 * 
 * at runtime: SelectWirelet<> on an extension Runtime class
 * 
 * For user space wirelets: BaseAssembly..
 * <p>
 * Note: Invoking this method remove every wirelet from this selection. But will also make the wirelet as well as any
 * other selections of the same container (or component) instance. Maaske det her skal staa i den gennerelle
 * dokumentation. Build-wirelets bliver jo proceseret paa et andet tidspunkt. Saa der er vel 2 regler..
 * 
 * 
 * <p>
 * It is consider an error to invoke more than a single method for a single instance. Unless the peek methods. Where you
 * can do what you want
 * 
 * @param <T>
 *            the type of wirelets in this selection
 * 
 * @implNote We expect the number of wirelets for a single component to be small in practice. So the current
 *           implementation will iterate through every wirelet specified when wiring the component on every operation on
 *           this interface. This is unlikely to effect performance.
 */

// selects but does not process the wirelets

// Hvordan fungere method foo(SelectWirelets<InjectWirelet> sw1, SelectWirelets<InjectWirelet> sw2)
// De skal ikke vaere i sw2... Eller hvad hvis man kalder sw2 foerst???
// Jo maaske er det bare den der iterere foerst... Ja selvf. Hvis man ikke kalder metoder sker
// der jo ikke noget alligevel, saa lav en test.

// Was SelectWirelets 

// Problemet med at have en onX on wireletten..
// Er at vi ikke kan finalize noget i constructeren... fordi den vil tage en constructed Extensor i onExtensor()
// Vi vil rigtig gerne have wireletten i constructoren paa extensoren...
@SuppressWarnings("rawtypes")
public sealed interface WireletSelection<W extends Wirelet> permits PackedWireletSelection {

    // l.orElse(w->w.launchMode, defaultLaunchmode);
    /**
     * 
     * Typically for extract a value for a wirelet and using that if present
     * 
     * @param <E>
     * @param mapper
     *            a mapper
     * @param orElse
     * @return
     */
    // shortcut for findLast().map(mapper).orElse(orElse)

    default List<W> peekAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Performs the given action for each wirelet in the selection. Unlike {@link #processForEach(Consumer)} this method does not
     * remove the wirelet from this selection or any other selection.
     * 
     * @param action
     *            the action to perform
     */
    void peekEach(Consumer<? super W> action);

    /** {@return whether or not this source contains any unconsumed wirelets. Does not consume} */
    boolean peekIsEmpty();

    /** {@return the number of unconsumed wirelets in this source, consuming each wirelet in the process} */
    int peekSize();

    /**
     * 
     * @param <E>
     * @param mapper
     *            the mapper to apply (if non-empty) to the last wirelet before returning the result
     * @param ifEmpty
     *            the value to return if the selection is empty
     * @return stuff
     */
    default <E> E processFindLastOrElse(Function<? super W, ? extends E> mapper, E ifEmpty) {
        requireNonNull(mapper, "mapper is null");
        Optional<W> result = processReturnLast();
        return result.isEmpty() ? ifEmpty : mapper.apply(result.get());
    }

    /**
     * Performs the given action for each wirelet in this selection. Removing each wirelet from this selection as well as
     * any other selection.
     * 
     * @param action
     *            the action to perform
     * @see #peekEach(Consumer)
     */
    void processForEach(Consumer<? super W> action); // return boolean = if any elements selected?

    /**
     * Returns the last wirelet in this selection or empty {@code Optional}, if no wirelets are present.
     * <p>
     * This is a <a href="package-summary.html#StreamOps">consumable operation</a>.
     * 
     * @return the last wirelet in this selection or empty {@code Optional}, if no wirelets are present
     */
    // consumeAllReturnLast
    // processReturnLast();
    // processReturnAll();
    // processForEach();
    Optional<W> processReturnLast();

    /** {@return every wirelet in this source as a list, consuming each wirelet in the process.} */
    default List<W> processReturnAll() {
        throw new UnsupportedOperationException();
    }

    /**
     * Returns an empty selection of wirelets.
     * 
     * @param <W>
     *            the type of wirelets in the selection
     * @return an empty wirelet selection
     */
    public static <W extends Wirelet> WireletSelection<W> of() {
        @SuppressWarnings("unchecked")
        WireletSelection<W> w = (WireletSelection<W>) PackedWireletSelection.EMPTY;
        return w;
    }

    // Hvad skal vi bruge den her til??? Testing primaert ville jeg mene...
    // Hvad med dem der ikke bliver consumet? skal vi have en WireletHandle.peekCount()???
    /**
     * This method is mainly used for testing purposes.
     * 
     * @param <W>
     *            the type of wirelets in the selection
     * @param wireletClass
     *            the type of wirelets in the selection
     * @param wirelets
     *            the wirelets to include in the selection if they are assignable to the specified {@code wireletClass}.
     * @return the selection
     */
    @SafeVarargs
    public static <W extends Wirelet> WireletSelection<W> of(Class<? extends W> wireletClass, Wirelet... wirelets) {
        return PackedWireletSelection.of(wireletClass, wirelets);
    }
}

//Hvorfor lige WireletList og ikke f.eks. List eller en annotering
//Fordi der er en masse special metoder... Som kun kan bruges her

//Vi dropper consume foran alt. Da det giver for lange navne.
//Istedet consumer alle metoder paanaer dem der starter med peek

//@DynamicInject

//Okay... Saa kan entent tage 
//ExtensionSetup
//ComponentSetup
//Component
//WireletReceiver
//WireletContainer/WireletHolder/WireletBag

//Do we want to have partial consumes at all??? I cannot think of any usecase.
//When it is not handled

//TODO
//Implement iterator
//Must have matching peek versions of everything (not sure, maybe just a single peek )

//ConsumeAll -> returns whether or not we have any match, useful for boolean is present wirelets
//ConsumeAll -> returns latest
//ConsumeAll -> Fails if more than 1.. (or is this in extension model... probably bad in extensions model.. We need to iterate though all every time then

//WireletSource
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

//// Det her var den helt gamle maade hvor vi angav paa wireletten  
//
////cannot be consumed individually. Only as either
////List or Set....
////Must be a super type of this wirelet type
////Is inherited
////Can only be a part of one aggregate type...
////And can only be injected as an aggregate type
//protected static final void $aggregateAs(Class<? extends Wirelet> wireletType) {
//WireletModel.bootstrap(StackWalkerUtil.SW.getCallerClass()).stackBy(wireletType);
//}
