package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.Wirelet;
import app.packed.component.WireletList;

// Lige nu bliver den brugt 3 steder fra.
// WireletHandle.of <- mainly for test
// ComponentSetup
// RuntimeSetup

/** Implementation of {@link WireletList}. */
public final /* primitive */ class PackedWireletHandle<W extends Wirelet> implements WireletList<W> {

    /** An empty handle used by {@link WireletList#of()}. */
    private static final PackedWireletHandle<?> EMPTY = new PackedWireletHandle<>();

    /** The type of wirelet's that will be handled. All other types are ignored */
    private final Class<? extends W> wireletClass;

    /** The wirelet wrapper containing the actual wirelets */
    private final WireletWrapper wirelets;

    /** Creates a new empty list. */
    @SuppressWarnings("unchecked")
    private PackedWireletHandle() {
        this.wirelets = WireletWrapper.EMPTY;
        this.wireletClass = (Class<? extends W>) Wirelet.class;
    }

    public PackedWireletHandle(WireletWrapper wirelets, Class<? extends W> wireletClass) {
        this.wirelets = wirelets;
        // We should check all public wirelet types here
        if (Wirelet.class == wireletClass) {
            throw new IllegalArgumentException("Cannot specify " + Wirelet.class.getSimpleName() + ".class");
        }
        this.wireletClass = requireNonNull(wireletClass, "wireletClass is null");
    }

    /** {@inheritDoc} */
    @Override
    public void consumeEach(Consumer<? super W> action) {
        consumeEach(wirelets, wireletClass, action);
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        int count = 0;
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                if (wireletClass.isInstance(ws[i])) {
                    count++;
                }
            }
        }
        return count;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        boolean result = true;
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletClass.isInstance(w)) {
                    return false;
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<W> last() {
        W result = null;
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletClass.isInstance(w)) {
                    if (result == null) {
                        result = (W) w;
                    }
                    ws[i] = null;
                    wirelets.unconsumed--;
                }
            }
        }
        return Optional.ofNullable(result);
    }

    @SuppressWarnings("unchecked")
    public static <W extends Wirelet> void consumeEach(WireletWrapper wrapper, Class<? extends W> wireletClass, Consumer<? super W> action) {
        requireNonNull(action, "action is null");
        if (wrapper.unconsumed > 0) {
            Wirelet[] ws = wrapper.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletClass.isInstance(w)) {
                    action.accept((W) w);
                    ws[i] = null;
                    wrapper.unconsumed--;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Wirelet> PackedWireletHandle<T> of() {
        return (PackedWireletHandle<T>) EMPTY;
    }

    public static <T extends Wirelet> WireletList<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        requireNonNull(wireletClass, "wireletClass is null");
        WireletWrapper wp = new WireletWrapper(WireletArray.flatten(wirelets));
        return new PackedWireletHandle<>(wp, wireletClass);
    }

    @Override
    public void peekEach(Consumer<? super W> action) {
        // TODO Auto-generated method stub
        
    }
}
