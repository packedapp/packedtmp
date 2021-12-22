package app.packed.lifetime;

import java.util.Optional;

import app.packed.component.ComponentMirror;

// En applications instance (Top Containeren
// En Bean Instance
// En Service?


// Services -> Require ApplicationLevel lifetime

// https://thesaurus.plus/related/life_cycle/lifetime
public interface Lifetime {

    static final Lifetime UNMANAGED = null; // Or Epheral

    ComponentMirror component();
    
    // was Set<Lifetime> parents();
    Optional<Lifetime> parent();
}
// Hvad hvis man vil lave Request lifetime i shutdown?
// 