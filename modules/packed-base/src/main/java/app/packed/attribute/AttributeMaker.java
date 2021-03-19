package app.packed.attribute;

import java.util.function.Function;
import java.util.function.Predicate;

public interface AttributeMaker<T> {

    <A> void add(Attribute<A> attribute, Function<T, A> mapper);
    <A> void optional(Attribute<A> attribute, Predicate<T> isPresent, Function<T, A> mapper);
    void scanClass(); // scan for annotations...
}
