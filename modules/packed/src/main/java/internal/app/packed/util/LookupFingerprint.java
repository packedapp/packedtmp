package internal.app.packed.util;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.ref.WeakReference;

public final class LookupFingerprint {
    private final int allowedModes;
    private final WeakReference<Class<?>> lookupClassRef;
    private final WeakReference<Class<?>> prevLookupClassRef;

    public LookupFingerprint(Lookup lookup) {
        this.allowedModes = lookup.lookupModes();
        this.lookupClassRef = new WeakReference<>(lookup.lookupClass());
        this.prevLookupClassRef = new WeakReference<>(lookup.previousLookupClass());
    }

    public boolean matches(Lookup other) {
        if (allowedModes != other.lookupModes())
            return false;

        Class<?> storedLookupClass = lookupClassRef.get();
        Class<?> storedPrevClass = prevLookupClassRef.get();

        // If classes match exactly (common case), return true
        return (storedLookupClass == other.lookupClass()) && (storedPrevClass == other.previousLookupClass());
    }
}