package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;

/**
 * A wirelet that allows for combining multiple wirelets into a single wirelet.
 * <p>
 * 
 * @see Wirelet
 */
public final /* primitive */ class WireletArray extends Wirelet {

    /** An empty wirelet array. */
    public static final Wirelet[] EMPTY = new Wirelet[0];

    /** The wirelets this wirelet wraps. Will never contain any WireletArray instances. */
    final Wirelet[] wirelets;

    /**
     * Create a new wirelet array.
     * 
     * @param wirelets
     *            the wirelets to wrap
     */
    private WireletArray(Wirelet[] wirelets) {
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

    static Wirelet[] flatten(Wirelet w1, Wirelet w2) {
        Wirelet[] result;
        if (w1 instanceof WireletArray wl1) {
            Wirelet[] wirelets1 = wl1.wirelets;
            if (w2 instanceof WireletArray wl2) {
                Wirelet[] wirelets2 = wl2.wirelets;
                result = new Wirelet[wirelets1.length + wirelets2.length];
                copyInto(wirelets2, result, wirelets1.length);
            } else {
                result = new Wirelet[1 + wl1.wirelets.length];
                result[wl1.wirelets.length] = w2;
            }
            return copyInto(wirelets1, result, 0);
        } else if (w2 instanceof WireletArray wl2) {
            Wirelet[] wirelets = wl2.wirelets;
            result = new Wirelet[1 + wirelets.length];
            result[0] = w1;
            return copyInto(wirelets, result, 1);
        } else {
            return new Wirelet[] { w1, w2 };
        }
    }

    public static Wirelet[] flatten(Wirelet[] wirelets) {
        requireNonNull(wirelets, "wirelets is null");
        return switch (wirelets.length) {
        case 0 -> EMPTY;
        case 1 -> {
            Wirelet w = w(wirelets, 0);
            yield w instanceof WireletArray wl ? wl.wirelets : new Wirelet[] { w };
        }
        case 2 -> flatten(w(wirelets, 0), w(wirelets, 1));
        default -> {
            int size = wirelets.length;
            for (int i = 0; i < wirelets.length; i++) {
                Wirelet w = w(wirelets, i);
                if (w instanceof WireletArray list) {
                    size += list.wirelets.length - 1;
                }
            }
            if (size == wirelets.length) {
                yield wirelets.clone();
            }
            Wirelet[] tmp = new Wirelet[size];
            int i = 0;
            for (Wirelet w : wirelets) {
                if (w instanceof WireletArray list) {
                    copyInto(list.wirelets, tmp, i);
                } else {
                    tmp[i++] = w;
                }
            }
            yield tmp;
        }
        };
    }

    public final static WireletArray of(Wirelet... wirelets) {
        return new WireletArray(WireletArray.flatten(wirelets));
    }

    public final static WireletArray of(Wirelet wirelet, Wirelet other) {
        return new WireletArray(WireletArray.flatten(wirelet, other));
    }

    private static Wirelet w(Wirelet[] wirelets, int index) {
        Wirelet w = wirelets[index];
        if (w == null) {
            throw new NullPointerException("Wirelets is null at index " + 0);
        }
        return w;
    }
}
