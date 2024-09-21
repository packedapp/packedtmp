package internal.app.packed.container.wirelets;

import static java.util.Objects.requireNonNull;

import app.packed.container.Wirelet;

/**
 * A special wirelet that combines multiple wirelets into a single wirelet.
 * <p>
 * Is exposed to end users via various methods on {@link Wirelet}.
 *
 * @see Wirelet#andThen(Wirelet)
 * @see Wirelet#andThen(Wirelet...)
 * @see Wirelet#beforeThis(Wirelet...)
 * @see Wirelet#combine(Wirelet...)
 */
public final class CompositeWirelet extends FrameworkWirelet {

    /** An empty wirelet array. */
    static final Wirelet[] EMPTY = new Wirelet[0];

    /**
     * The wirelets that have been combined. The array have been checked for null values. And every wirelet have been
     * flattened (removal of recursive versions of CompositeWirelet) before storing in this field.
     */
    public final Wirelet[] wirelets;

    /**
     * Create a new wirelet array.
     *
     * @param wirelets
     *            the flattened wirelets to wrap
     */
    private CompositeWirelet(Wirelet[] wirelets) {
        this.wirelets = requireNonNull(wirelets, "wirelets is null");
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (wirelets.length > 0) {
            sb.append(wirelets[0]);
            for (int i = 1; i < wirelets.length; i++) {
                sb.append(", ").append(wirelets[i]);
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Copies multiple wirelets from one array to another array.
     *
     * @param source
     *            the source to copy from
     * @param dest
     *            the destination
     * @param destPosition
     *            the destination position
     * @return the destination
     */
    private static Wirelet[] copyInto(Wirelet[] source, Wirelet[] dest, int destPosition) {
        for (int i = 0; i < source.length; i++) {
            dest[i + destPosition] = source[i];
        }
        return dest;
    }

    private static Wirelet[] flatten1(Wirelet w) {
        return w instanceof CompositeWirelet wl ? wl.wirelets : new Wirelet[] { w };
    }

    public static Wirelet[] flatten2(Wirelet w1, Wirelet w2) {
        Wirelet[] result;
        if (w1 instanceof CompositeWirelet wl1) {
            Wirelet[] wirelets1 = wl1.wirelets;
            if (w2 instanceof CompositeWirelet wl2) {
                Wirelet[] wirelets2 = wl2.wirelets;
                result = new Wirelet[wirelets1.length + wirelets2.length];
                copyInto(wirelets2, result, wirelets1.length);
            } else {
                result = new Wirelet[1 + wl1.wirelets.length];
                result[wl1.wirelets.length] = w2;
            }
            return copyInto(wirelets1, result, 0);
        } else if (w2 instanceof CompositeWirelet wl2) {
            Wirelet[] wirelets = wl2.wirelets;
            result = new Wirelet[1 + wirelets.length];
            result[0] = w1;
            return copyInto(wirelets, result, 1);
        } else {
            return new Wirelet[] { w1, w2 };
        }
    }

    public static Wirelet[] flattenAll(Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        return switch (wirelets.length) {
        case 0 -> EMPTY;
        case 1 -> flatten1(nullChecked(wirelets, 0));
        case 2 -> flatten2(nullChecked(wirelets, 0), nullChecked(wirelets, 1));
        default -> {
            int size = wirelets.length;
            for (int i = 0; i < wirelets.length; i++) {
                Wirelet w = nullChecked(wirelets, i);
                if (w instanceof CompositeWirelet list) {
                    size += list.wirelets.length - 1;
                }
            }
            if (size == wirelets.length) {
                yield wirelets.clone();
            }
            Wirelet[] tmp = new Wirelet[size];
            int i = 0;
            for (Wirelet w : wirelets) {
                if (w instanceof CompositeWirelet list) {
                    copyInto(list.wirelets, tmp, i);
                } else {
                    tmp[i++] = w;
                }
            }
            yield tmp;
        }
        };
    }

    private static Wirelet nullChecked(Wirelet[] wirelets, int index) {
        Wirelet w = wirelets[index];
        if (w == null) {
            throw new NullPointerException("Wirelets is null at index " + 0);
        }
        return w;
    }

    /**
     * Combines multiple wirelets into a single wirelet. Flattens the wirelets in the process
     *
     * @param wirelets
     *            the wirelets to combine
     * @return the combined wirelet
     * @see Wirelet#combine(Wirelet...)
     */
    public final static Wirelet of(Wirelet... wirelets) {
        return new CompositeWirelet(CompositeWirelet.flattenAll(wirelets));
    }

    /**
     * @param wirelet
     * @param other
     * @return the combined wirelet
     * @see Wirelet#andThen(Wirelet)
     * @see Wirelet#andThen(Wirelet...)
     * @see Wirelet#beforeThis(Wirelet...)
     */
    public static Wirelet of(Wirelet wirelet, Wirelet other) {
        return new CompositeWirelet(CompositeWirelet.flatten2(wirelet, other));
    }
}
