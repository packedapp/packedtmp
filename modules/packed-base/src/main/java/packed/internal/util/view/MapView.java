package packed.internal.util.view;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

// De var en fucking god plan...
// Null is never supported
public interface MapView<K, V> extends Iterable<Map.Entry<K, V>> {

    Map<K, V> asMap();

    Optional<V> find(K key);

    <W> MapView<K, W> mapValue(Function<? super V, ? extends W> mapper);

    V get(K key);

    MapView<K, V> materialize();
}
