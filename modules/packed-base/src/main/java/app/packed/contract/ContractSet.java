package app.packed.contract;

import java.util.Optional;

// Ideen er lidt at vi f.eks. kan tilfoeje en print metoder en toDoc metode
// Og maaske nogle maader hvor vi kan printe diffencen
// ContractDiff er nok mere en toDoc ting

interface ContractSet {

    boolean contains(Class<? extends Contract> contractType);

    <C extends Contract> Optional<C> find(Class<C> contractType);
    
    <C extends Contract> C use(Class<C> contractType);
}
