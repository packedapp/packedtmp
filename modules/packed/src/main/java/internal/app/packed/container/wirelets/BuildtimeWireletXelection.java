package internal.app.packed.container.wirelets;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Consumer;

import app.packed.container.Wirelet;

// Lige nu bliver den brugt 3 steder fra.
// WireletHandle.of <- mainly for test
// ContainerSetup
// RuntimeSetup

/** Implementation of {@link WireletSelection}. */
public final /* primitive */ class BuildtimeWireletXelection<W extends Wirelet> implements OldWireletSelection<W> {

    /** An empty selection used by {@link WireletSelection#of()}. */
    public static final BuildtimeWireletXelection<?> EMPTY = new BuildtimeWireletXelection<>();

    /** The wirelet wrapper containing the actual wirelets */
    private final WireletWrapper wirelets;

    /** The type of wirelet's that will be handled. All other types are ignored */
    private final Class<? extends W> wireletType;

    /** Creates a new empty selection. */
    @SuppressWarnings("unchecked")
    private BuildtimeWireletXelection() {
        this.wirelets = WireletWrapper.EMPTY;
        this.wireletType = (Class<? extends W>) Wirelet.class;
    }

    public BuildtimeWireletXelection(WireletWrapper wirelets, Class<? extends W> wireletType) {
        this.wirelets = wirelets;
        // We should check all public wirelet types here
        this.wireletType = requireNonNull(wireletType, "wireletType is null");
        if (Wirelet.class == wireletType) {
            throw new IllegalArgumentException("Cannot specify " + Wirelet.class.getSimpleName() + ".class");
        }
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public Optional<W> processReturnLast() {
        W result = null;
        if (wirelets.unconsumed > 0) {
            Wirelet[] ws = wirelets.wirelets;
            for (int i = 0; i < ws.length; i++) {
                Wirelet w = ws[i];
                if (wireletType.isInstance(w)) {
                    result = (W) w;
                    ws[i] = null;
                    wirelets.unconsumed--;
                }
            }
        }
        return Optional.ofNullable(result);
    }

    /** {@inheritDoc} */
    @Override
    public void processEach(Consumer<? super W> action) {
        consumeEach(wirelets, wireletType, action);
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void peekEach(Consumer<? super W> action) {
        requireNonNull(action, "action is null");
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletType.isInstance(w)) {
                    action.accept((W) w);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean peekIsEmpty() {
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletType.isInstance(w)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public int peekSize() {
        int count = 0;
        if (wirelets.unconsumed > 0) {
            for (Wirelet w : wirelets.wirelets) {
                if (wireletType.isInstance(w)) {
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

    public static <T extends Wirelet> OldWireletSelection<T> of(Class<? extends T> wireletClass, Wirelet... wirelets) {
        requireNonNull(wireletClass, "wireletClass is null");
        WireletWrapper wp = new WireletWrapper(CompositeWirelet.flattenAll(wirelets));
        return new BuildtimeWireletXelection<>(wp, wireletClass);
    }
}
