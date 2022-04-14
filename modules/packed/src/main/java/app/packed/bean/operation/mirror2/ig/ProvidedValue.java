package app.packed.bean.operation.mirror2.ig;

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
