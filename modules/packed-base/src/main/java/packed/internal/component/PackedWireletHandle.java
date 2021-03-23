package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;

// Lige nu bliver den brugt 3 steder fra.
// WireletHandle.of <- mainly for test
// ComponentSetup
// RuntimeSetup

/** Implementation of {@link WireletHandle}. */
public final /* primitive */ class PackedWireletHandle<W extends Wirelet> implements WireletHandle<W> {

    /** An empty handle used by {@link WireletHandle#of()}. */
    private static final PackedWireletHandle<?> EMPTY = new PackedWireletHandle<>();

    private final Class<? extends W> wireletClass;

    private final WireletWrapper wirelets;

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
    public int count() {
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
    public boolean isAbsent() {
        boolean result = true;
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletClass.isInstance(w)) {
                    result = false;
                }
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        return !isAbsent();
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
    public static <W extends Wirelet> void consumeEach(WireletWrapper wirelets, Class<? extends W> wireletClass, Consumer<? super W> action) {
        requireNonNull(action, "action is null");
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletClass.isInstance(w)) {
                    action.accept((W) w);
                    ws[i] = null;
                    wirelets.unconsumed--;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Wirelet> WireletHandle<T> of() {
        return (WireletHandle<T>) EMPTY;
    }

    public static <T extends Wirelet> WireletHandle<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        requireNonNull(wireletClass, "wireletClass is null");
        WireletWrapper wp = new WireletWrapper(WireletArray.flatten(wirelets));
        return new PackedWireletHandle<>(wp, wireletClass);
    }
}
