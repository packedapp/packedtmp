package app.packed.bean;

import java.util.function.Function;

public interface BeanDriver {

    
    
    /* sealed */ interface Builder {

        BeanDriver build();

        // Specific super type

        // Den kan vi jo se fra Typen af configuration...
        Builder kind(BeanKind kind);

        Builder namePrefix(Function<Class<?>, String> computeIt);

        Builder namePrefix(String prefix);

        Builder noInstances();

        // BeanConfigurationBinder<BeanConfiguration> buildBinder();
        Builder noReflection();

        Builder oneInstance();

        // Vi kan ikke rejecte extensions paa bean niveau...
        //// Man kan altid lave en anden extension som bruger den extension jo
        //// Saa det er kun paa container niveau vi kan forbyde extensions
        
        //// For instantiationOnly
        // reflectOnConstructorOnly();

        // reflectOn(Fields|Methods|Constructors)
        // look in declaring class
    }
}
