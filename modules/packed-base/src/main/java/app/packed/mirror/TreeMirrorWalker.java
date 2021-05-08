package app.packed.mirror;

import java.util.stream.Stream;

// Only stream a walker can be used multiple times...
// All the rationale for Stream does not apply here...
// https://stackoverflow.com/questions/28459498/why-are-java-streams-once-off#:~:text=Unlike%20C%23's%20IEnumerable,away%20a%20lot%20of%20power.

// I mean 
public interface TreeMirrorWalker<T extends Mirror> {

    // s.s.s.assertEmpty vs
    // assert s.s.s.isEmpty();
    // IDK tror ikke jeg har det med...
    void assertEmpty();
    void assertNotEmpty();
    
    T any();
    
    T one(); // exactlyOne (or fail)

    Stream<T> stream();

    Stream<T> streamBreathFirst();

    Stream<T> streamDepthFirst();
}

// StreamModel.allOf(C)