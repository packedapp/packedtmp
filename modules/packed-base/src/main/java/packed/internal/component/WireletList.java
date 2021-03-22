package packed.internal.component;

import static java.util.Objects.requireNonNull;

import app.packed.component.Wirelet;

public final /* primitive */ class WireletList extends Wirelet {
    
    static final Wirelet[] EMPTY = new Wirelet[0];

    final Wirelet[] wirelets;

    public WireletList(Wirelet[] wirelets) {
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

    public static Wirelet[] flatten(Wirelet w1, Wirelet w2) {
        Wirelet[] result;
        if (w1 instanceof WireletList wl1) {
            if (w2 instanceof WireletList wl2) {
                result = new Wirelet[wl1.wirelets.length + wl2.wirelets.length];
                insertInto(result, wl1.wirelets.length, wl2);
            } else {
                result = new Wirelet[1 + wl1.wirelets.length];
                result[result.length - 1] = w2;
            }
            return insertInto(result, 0, wl1);
        } else if (w2 instanceof WireletList wl2) {
            result = new Wirelet[1 + wl2.wirelets.length];
            result[0] = w1;
            return insertInto(result, 1, wl2);
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
            yield w instanceof WireletList wl ? wl.wirelets : new Wirelet[] { w };
        }
        case 2 -> flatten(w(wirelets, 0), w(wirelets, 1));
        default -> {
            int size = wirelets.length;
            for (int i = 0; i < wirelets.length; i++) {
                Wirelet w = w(wirelets, i);
                if (w instanceof WireletList list) {
                    size += list.wirelets.length - 1;
                }
            }
            if (size == wirelets.length) {
                yield wirelets.clone();
            }
            Wirelet[] tmp = new Wirelet[size];
            int i = 0;
            for (Wirelet w : wirelets) {
                if (w instanceof WireletList list) {
                    insertInto(tmp, i, list);
                } else {
                    tmp[i++] = w;
                }
            }
            yield tmp;
        }
        };
    }

    private static Wirelet[] insertInto(Wirelet[] insertInto, int index, WireletList list) {
        Wirelet[] ws = list.wirelets;
        for (int i = 0; i < ws.length; i++) {
            insertInto[i + index] = ws[i];
        }
        return insertInto;
    }

    private static Wirelet w(Wirelet[] wirelets, int index) {
        Wirelet w = wirelets[index];
        if (w == null) {
            throw new NullPointerException("Wirelets is null at index " + 0);
        }
        return w;
    }
}
