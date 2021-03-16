package app.packed.inject.sandbox;

// Lidt taenkt som en advanced resultat fra
// @Provide, 
// bootstrapProvide...

interface ProvidedValue<T> {

    static <T> ProvidedValue<T> of(T value) {
        return null;
    }
    
    static <T> ProvidedValue<T> missing() {
        return null;
    }

    static <T> ProvidedValue<T> nullable() {
        return null;
    }
}
