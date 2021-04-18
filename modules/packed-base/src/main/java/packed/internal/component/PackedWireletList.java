package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.Wirelet;
import app.packed.component.WireletSource;

// Lige nu bliver den brugt 3 steder fra.
// WireletHandle.of <- mainly for test
// ComponentSetup
// RuntimeSetup

/** Implementation of {@link WireletSource}. */
public final /* primitive */ class PackedWireletList<W extends Wirelet> implements WireletSource<W> {

    /** An empty handle used by {@link WireletSource#of()}. */
    public static final PackedWireletList<?> EMPTY = new PackedWireletList<>();

    /** The type of wirelet's that will be handled. All other types are ignored */
    private final Class<? extends W> wireletClass;

    /** The wirelet wrapper containing the actual wirelets */
    private final WireletWrapper wirelets;

    /** Creates a new empty list. */
    @SuppressWarnings("unchecked")
    private PackedWireletList() {
        this.wirelets = WireletWrapper.EMPTY;
        this.wireletClass = (Class<? extends W>) Wirelet.class;
    }

    public PackedWireletList(WireletWrapper wirelets, Class<? extends W> wireletClass) {
        this.wirelets = wirelets;
        // We should check all public wirelet types here
        if (Wirelet.class == wireletClass) {
            throw new IllegalArgumentException("Cannot specify " + Wirelet.class.getSimpleName() + ".class");
        }
        this.wireletClass = requireNonNull(wireletClass, "wireletClass is null");
    }

    /** {@inheritDoc} */
    @Override
    public void forEach(Consumer<? super W> action) {
        consumeEach(wirelets, wireletClass, action);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEmpty() {
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletClass.isInstance(w)) {
                    return false;
                }
            }
        }
        return true;
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

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void peekEach(Consumer<? super W> action) {
        requireNonNull(action, "action is null");
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletClass.isInstance(w)) {
                    action.accept((W) w);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int size() {
        int count = 0;
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletClass.isInstance(w)) {
                    count++;
                }
            }
        }
        return count;
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

    public static <T extends Wirelet> WireletSource<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        requireNonNull(wireletClass, "wireletClass is null");
        WireletWrapper wp = new WireletWrapper(WireletArray.flatten(wirelets));
        return new PackedWireletList<>(wp, wireletClass);
    }
}
