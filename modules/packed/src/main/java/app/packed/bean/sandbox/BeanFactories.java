package app.packed.bean.sandbox;

import java.lang.invoke.MethodHandles.Lookup;

import app.packed.base.Key;
import app.packed.inject.Factory;

// Laver specielle factories som nogen gang er brugbare.
// Her taenkes specielt paa situationer hvor vi nu ikke kan bruge wirelets

// Smid den paa BeanSupport syntes jeg...


// Men ogsaa bare findInjectable()

// BeanFactories
final class BeanFactories {

    BeanFactories() {
        
    }
    
    // Altsaa det er jo lidt som wirelets...
    
    
    // Open up for the whole bean
    // Tror vi skal have 2 forskellige. Inde der aabner selve funktionen
    // og en der aabner helebeanen
    public <T> Factory<T> openUp(Factory<T> factory, Lookup lookup) {
        
        // Altsaa Factory.ofConstant(xx).lookup(Lookup)
        // Giver jo ikke adgang til alle metoder felter osv
        
        throw new UnsupportedOperationException();
    }
    
    public Factory<?> setDefaultKey(Factory<?> fac, Key<?> key) {
        // provide(factory)
        throw new UnsupportedOperationException();
    }
    
    // Maaske kan vi wrappe existerende factories?
}
