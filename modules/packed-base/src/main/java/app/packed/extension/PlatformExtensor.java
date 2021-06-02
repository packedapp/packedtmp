package app.packed.extension;

import java.util.Set;

//One per JVM
/**
 * A single instance of this class is installed per JVM. Typically some kind of shared resource...
 * <p>
 * What can it do that it is not as easy to do Things that it can do
 * 
 * Schedule annotations...
 * 
 * Maaske noget JFR
 * 
 * JMX managed beans...
 */

public abstract class PlatformExtensor<E extends Extension> extends Extensor<E> {

    protected ExtensorMirror mirror() {
        // Det er jo saa lige pludselig et runtime mirror...
        throw new UnsupportedOperationException();
    }

    public static Set<ExtensorMirror> all() {
        throw new UnsupportedOperationException();
    }
}

// track instances
// linger X
