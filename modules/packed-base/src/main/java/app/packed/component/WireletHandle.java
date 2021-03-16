package app.packed.component;

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

    WireletHandle<T> noConsume();

    /**
     * Returns the number of wirelets that this handle contain.
     * 
     * @return
     */
    int size();

    // forEach
    // consume
    T take(); // one() maybe. Emphasize at man consumer en...

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
        w.take();
    }
}