package app.packed.bean;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.inject.Factory;

// Laver specielle factories som nogen gang er brugbare.
// Her taenkes specielt paa situationer hvor vi nu ikke kan bruge wirelets

// Smid den paa BeanSupport syntes jeg...


// Men ogsaa bare findInjectable()
final class BeanFactory {

    BeanFactory() {
        
    }
    
    // Open up for the whole bean
    public Factory<?> openUp(Factory<?> fac, Lookup lookup) {
        throw new UnsupportedOperationException();
    }
    
    
    // Maaske kan vi wrappe existerende factories?
}
