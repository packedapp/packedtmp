package app.packed.extension.sandbox.convert;

import java.util.function.Function;

import app.packed.extension.ExtensionService;

// Den eneste ting jeg har... er at man maaske har lyst til at kalkulere et eller andet paa baggrund
// af receiveren...

// boostrap class


@ExtensionService(extension = ConvExtension.class, implementation = ConvManagerImpl.class)
public interface ConvManager {
    <T> T convert(Object from, Class<T> convertTo);
    
    
    // Factory<ConvManagerImpl> bootstrapClass(Callsite) {
    // Factory<ConvManagerImpl> bootstrapClassWithComponent(Callsite) {
    //}
}

record ConvManagerImpl(ConvExtensor e) implements ConvManager {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <T> T convert(Object from, Class<T> convertTo) {
        Function f = e.m.get(from.getClass());
        return (T) f.apply(from);
    }
}