package app.packed.inject;

import java.util.List;

// Altsaa vi har maaske 2 ting vi er interesseret i... Attributer
// Og hvordan er servicen blevet resolvet
// Men maaske skal det ikke vaere paa samme interface...
// Det omkring hvordan servicen er blevet resolvet er helt sikkert bare
// et debug scenario.. Men maaske kan InjectionContext godt staa alene

// Men den kunne godt vaere god 

// Skal selvfoelgelig ikke hedde provider.. Hvis den fungere med en tom service...
// ServiceFuture?
interface ServiceProvider<T> extends Provider<T> /* extends AttributeHolder */ {

    // Includes detailed information about the service and how we got there...
}

class Tester {

    // Selv hvis List ikke eksistere, saa fungere det alligevel...
    // ServiceProvider.provide() smider bare en exception...
    public Tester(String s, ServiceProvider<List<?>> sp) {

    }
}