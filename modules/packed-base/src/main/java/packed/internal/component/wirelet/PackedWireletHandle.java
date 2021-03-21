package packed.internal.component.wirelet;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.component.Wirelet;
import app.packed.component.WireletHandle;
import packed.internal.component.wirelet.WireletPack.ConsumableWirelet;

/** Implementation of {@link WireletHandle}. */
public final /* primitive */ class PackedWireletHandle<W extends Wirelet> implements WireletHandle<W> {

    /** An empty handle used by {@link WireletHandle#of()}. */
    private static final PackedWireletHandle<?> EMPTY = new PackedWireletHandle<>();

    private final Class<? extends W> wireletClass;

    private final WireletPack wirelets;

    @SuppressWarnings("unchecked")
    private PackedWireletHandle() {
        this.wirelets = WireletPack.EMPTY;
        this.wireletClass = (Class<? extends W>) Wirelet.class;
    }

    PackedWireletHandle(WireletPack wirelets, Class<? extends W> wireletClass) {
        this.wirelets = wirelets;
        // We should check all public wirelet types here
        if (Wirelet.class == wireletClass) {
            throw new IllegalArgumentException("Cannot specify " + Wirelet.class.getSimpleName() + ".class");
        }
        this.wireletClass = requireNonNull(wireletClass, "wireletClass is null");
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void forEach(Consumer<? super W> action) {
        requireNonNull(action, "action is null");
        for (ConsumableWirelet e : wirelets.list) {
            if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                action.accept((W) e.wirelet);
                e.isReceived = true;
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAbsent() {
        boolean result = true;
        for (ConsumableWirelet e : wirelets.list) {
            if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                result = false;
                e.isReceived = true;
            }
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPresent() {
        return !isAbsent();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<W> last() {
        W result = null;
        for (ConsumableWirelet e : wirelets.list) {
            if (!e.isReceived && wireletClass.isInstance(e.wirelet)) {
                if (result == null) {
                    result = (W) e.wirelet;
                }
                e.isReceived = true;
            }
        }
        return Optional.ofNullable(result);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Wirelet> WireletHandle<T> of() {
        return (WireletHandle<T>) EMPTY;
    }

    public static <T extends Wirelet> WireletHandle<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        requireNonNull(wireletClass, "wireletClass is null");
        requireNonNull(wirelets, "wirelets is null");
        if (wirelets.length == 0) {
            return of();
        }
        return WireletPack.create(null, wirelets).handleOf(wireletClass.getModule(), wireletClass);
    }
}
