package packed.internal.container;

// Maaske lukker den kun ned i forhold til platform namespaces

// Ideen er at kunne gemme og faa injected services med
// platform scope, og hvor der bliver rengjoert naar JVM'en
// bliver lukket ned
final class PlatformExtensionSupport {

    static final ClassValue<Entry> ENTRIES = new ClassValue<>() {

        @Override
        protected Entry computeValue(Class<?> type) {
            // TODO Auto-generated method stub
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> type) {
        Entry e = ENTRIES.get(type);
        return (T) e.instance;
    }

    static class Entry {
        Object instance;
    }
}
