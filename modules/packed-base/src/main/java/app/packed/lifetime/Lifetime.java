package app.packed.lifetime;

import java.util.Set;

// Services -> Require ApplicationLevel lifetime

// https://thesaurus.plus/related/life_cycle/lifetime
public interface Lifetime {

    /**  */
    static final Lifetime APPLICATION = null;

    static final Lifetime UNMANAGED = null; // Or Epheral

    Set<Lifetime> parents();
}
// Hvad hvis man vil lave Request lifetime i shutdown?
// 