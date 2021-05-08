package app.packed.mirror;

import java.util.Set;
import java.util.function.Predicate;

public interface MirrorSet<T extends Mirror> extends Iterable<T> {

    Set<T> asSet();

    boolean anyMatch(Predicate<? super T> predicate);

    boolean allMatch(Predicate<? super T> predicate);
}
