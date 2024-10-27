package app.packed.binding.sandbox;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

import app.packed.binding.Provider;
import internal.app.packed.ValueBased;
import internal.app.packed.util.PackedAnnotationList;
import internal.app.packed.util.StringFormatter;

/** The internal representation of a Key. */
@ValueBased
public record KeyInternal(Type type, PackedAnnotationList qualifiers, int hash) {

    /** Various classes that are not allowed as the type part of a key. */
    public static final Set<Class<?>> FORBIDDEN_KEY_TYPES = Set.of(Optional.class, OptionalDouble.class, OptionalInt.class, OptionalLong.class, Void.class,
            Provider.class, Key.class);

    public String toString(boolean longFormat) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key<");
        if (isQualified()) {
            if (longFormat) {
                sb.append(StringFormatter.format(qualifiers.annotations()[0]));
            } else {
                sb.append(StringFormatter.formatSimple(qualifiers.annotations()[0]));
            }
        }
        if (longFormat) {
            sb.append(StringFormatter.format(type));
        } else {
            sb.append(StringFormatter.formatSimple(type));
        }
        return sb.append(">").toString();
    }

    public KeyInternal withoutQualifiers() {
        return new KeyInternal(type, PackedAnnotationList.EMPTY, type.hashCode());
    }

    boolean isQualified() {
        return !qualifiers().isEmpty();
    }
}