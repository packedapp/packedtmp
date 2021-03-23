package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;

/** A wirelet that allows for combining multiple wirelets into a single wirelet. */
public final /* primitive */ class WireletArray extends Wirelet {

    /** An empty wirelet array. */
    static final Wirelet[] EMPTY = new Wirelet[0];

    /** The wirelets this wirelet wraps. */
    final Wirelet[] wirelets;

    /**
     * Create a new wirelet array.
     * 
     * @param wirelets
     *            the wirelets to wrap
     */
    public WireletArray(Wirelet[] wirelets) {
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
    private static Wirelet[] copyInto(WireletArray source, Wirelet[] dest, int destPosition) {
        Wirelet[] ws = source.wirelets;
        for (int i = 0; i < ws.length; i++) {
            dest[i + destPosition] = ws[i];
        }
        return dest;
    }

    public static Wirelet[] flatten(Wirelet w1, Wirelet w2) {
        Wirelet[] result;
        if (w1 instanceof WireletArray wl1) {
            Wirelet[] wirelets1 = wl1.wirelets;
            if (w2 instanceof WireletArray wl2) {
                Wirelet[] wirelets2 = wl2.wirelets;
                result = new Wirelet[wirelets1.length + wirelets2.length];
                toArray0(wirelets2, result, wirelets1.length);
            } else {
                result = new Wirelet[1 + wl1.wirelets.length];
                result[wl1.wirelets.length] = w2;
            }
            return toArray0(wirelets1, result, 0);
        } else if (w2 instanceof WireletArray wl2) {
            Wirelet[] wirelets = wl2.wirelets;
            result = new Wirelet[1 + wirelets.length];
            result[0] = w1;
            return toArray0(wirelets, result, 1);
        } else {
            return new Wirelet[] { w1, w2 };
        }
    }

    static Wirelet[] flatten(Wirelet w1, WireletArray w2) {
        Wirelet[] result;
        int i = 1;
        if (w1 instanceof WireletArray wa) {
            i = wa.wirelets.length;
            result = new Wirelet[wa.wirelets.length + w2.wirelets.length];
            copyInto(wa, result, 0);
        } else {
            result = new Wirelet[1 + w2.wirelets.length];
            result[0] = w1;
        }
        return copyInto(w2, result, i);
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
                    copyInto(list, tmp, i);
                } else {
                    tmp[i++] = w;
                }
            }
            yield tmp;
        }
        };
    }

    static Wirelet[] toArray(Wirelet w, Wirelet[] wirelets) {
        Wirelet[] result = new Wirelet[1 + wirelets.length];
        result[0] = w;
        return toArray0(wirelets, result, 1);
    }

    static Wirelet[] toArray(Wirelet[] w1, Wirelet[] w2) {
        Wirelet[] result = new Wirelet[w1.length + w2.length];
        toArray0(w1, result, 0);
        return toArray0(w1, result, w1.length);
    }

    private static Wirelet[] toArray0(Wirelet[] source, Wirelet[] dest, int destPosition) {
        for (int i = 0; i < source.length; i++) {
            dest[i + destPosition] = source[i];
        }
        return dest;
    }

    private static Wirelet w(Wirelet[] wirelets, int index) {
        Wirelet w = wirelets[index];
        if (w == null) {
            throw new NullPointerException("Wirelets is null at index " + 0);
        }
        return w;
    }
}
