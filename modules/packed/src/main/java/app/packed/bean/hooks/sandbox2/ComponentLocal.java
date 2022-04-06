package app.packed.bean.hooks.sandbox2;

// Ideen er vi laver noget a.la. ThreadLocal...
//bare med com

// Maaske den overskrives med Wirelets... Config ect...

// Vi har et array hvor vi gennem den samme med selve localen
//[... CompLocal ActualVAL ....]
// Og saa soerger man recursivt...

// Use AtomicReference if mutable... Eller ogsaa skal vi bare have en 
// MutableComponentInstanceLocal.. eller saa kan folk tro man ogsaa
// kan indsaette senere maaske

// ComponentInstanceLocal
public abstract class ComponentLocal<T> {

    @Deprecated
    protected void cleanup(T t) {
        //IDK...
        //Saa kan jo vi jo ligepludselig ikke bruge dem med stateless
    }
    
    // Altsaa det er vel component instance og ikke component??
    // F.eks. hvis vi har sessions containere der er defineret via et image...
    // og hvad goer vi med statiske componenter...

}


class Wirelets2 {

    protected static final void connectTo(ComponentLocal<?> local) {
       // Ideen er lidt at man kan bruge wireletten naar man vil...
    }
}
