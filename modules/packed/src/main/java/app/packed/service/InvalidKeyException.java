package app.packed.service;

// Hvis vi ikke kan lave en valid key...
// F.eks. Optional<

// Eneste problem er at Provider ikke er en .base type.. Saa syntes maaske ikke rigtigt det kan vaere en
// reserveret type.

// <T> T provide() <--- T is a free type variable. Cannot be converted to key


public class InvalidKeyException extends RuntimeException {

    /** */
    private static final long serialVersionUID = 1L;

}
