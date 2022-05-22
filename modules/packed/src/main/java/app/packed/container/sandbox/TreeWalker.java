package app.packed.container.sandbox;

import java.util.function.Consumer;
import java.util.stream.Stream;

// Only stream a walker can be used multiple times...
// All the rationale for Stream does not apply here...
// https://stackoverflow.com/questions/28459498/why-are-java-streams-once-off#:~:text=Unlike%20C%23's%20IEnumerable,away%20a%20lot%20of%20power.

// I mean 
// Altsaa er det istedet for en slags cursor ting...
// TreeCursor
public interface TreeWalker<T> {

    // s.s.s.assertEmpty vs
    // assert s.s.s.isEmpty();
    // IDK tror ikke jeg har det med...
    void assertEmpty();
    void assertNotEmpty();
    
    void forEach(Consumer<T> action);
    T toList();
    
    T any();
    
    T one(); // exactlyOne (or fail)

    Stream<T> stream();

    Stream<T> streamBreathFirst();

    Stream<T> streamDepthFirst();
}

// StreamModel.allOf(C)


// StatisticalModelAssembly {}