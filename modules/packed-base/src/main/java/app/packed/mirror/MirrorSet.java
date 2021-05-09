package app.packed.mirror;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

// Ideen er lidt at have en saet type der er optimeret for immutability
// Og for at skrive tests
// dodod.anyMatch();
public interface MirrorSet<T extends Mirror> extends Iterable<T> {

    default boolean allMatch(Predicate<? super T> predicate) {
        return stream().allMatch(predicate);
    }
    
    default boolean anyMatch(Predicate<? super T> predicate) {
        return stream().anyMatch(predicate);
    }
    
    Set<T> asSet();

    // exactly one or fail
    T one();
    
    Stream<T> stream();
}

//default void assertAllMatch(Predicate<? super T> predicate, String msg) {
//    if (!stream().allMatch(predicate)) {
//        throw new AssertionError(msg);
//    }
//}