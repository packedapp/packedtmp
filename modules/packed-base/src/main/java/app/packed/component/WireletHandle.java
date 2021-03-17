package app.packed.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Consumer;

// Det gode ved den her er at den jo samtidig fungere som
// en optional faetter
// WireletContainer/WireletHolder/WireletBag
// @DynamicInject

// Okay... Saa kan entent tage 
// ExtensionSetup
// ComponentSetup
// Component
// WireletReceiver
public /* sealed */ interface WireletHandle<T extends Wirelet> {

    void forEach(Consumer<? super T> action);

    /**
     * Returns whether or not this handle contains any wirelets
     * 
     * @return
     */
    default boolean isEmpty() {
        return size() == 0;
    }

    WireletHandle<T> peek();

    /**
     * Returns the number of wirelets that this handle contain.
     * 
     * @return
     */
    int size();

    // forEach
    // will consume any matching wirelet and return the last one...
    T last(); // one() maybe. Emphasize at man consumer en...

    static <T extends Wirelet> WireletHandle<T> of() {
        throw new UnsupportedOperationException();
    }

    static <T extends Wirelet> WireletHandle<T> of(T wirelet) {
        throw new UnsupportedOperationException();
    }
}

class ZMyWirelet extends Wirelet {
    final String val = "asasd";
}

class ZUsage {

    public void foo(WireletHandle<ZMyWirelet> w) {
        w.forEach(c -> System.out.println(c.val));
        w.last();
    }
}

/**
 * Attempts to find a wirelet of targets type.
 */
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// Collect, Receive, Accept, Consume

// Grunden til jeg ikke kan lide WireletInject er den kan puttes paa en parameter...
// Men det kan @Inject ikke.
// @Nullable, Optional, List
// VarHandle, MethodHandle doesn't really work with WireletHandle...
// methods are conditional invoked.....

// Hmm Hvad hvis jeg har foo(Optional<EEE>)

// Skal den virkelig invokes alligevel???
//  
// @WireletLink...  Nah @WireletLink Optional<>

// Den her doede fordi vi ikke kan lide UseWirelet...
@interface UseWirelet {}

/// Metode??? det giver jo god mening...
/// Men maaske hellere i forbindelse med @Initialize