package app.packed.service;

//IDK

// Det gode ved at have en seperat export er

// Vi kan lave en custom type som returnere noget andet
// Vi kan exportere 
@interface Export {
    Class<?>[] as() default {};
    
    // If empty it takes the value of @Provide
    // Used standalone either this single value or defaults to Prototype
    Provide2.Mul[] sd() default {};
}

@interface Provide2 {
    
    Mul value() default Mul.NO_CACHING;
    
    enum Mul {
        CONSTANT,
        LAZY,
        NO_CACHING;
    }
}

// visibility is an array to allow it to be used with @Provide
// In which it will use the value
// If it is used standalone without @Provide Mul 
@interface Exported2 {
    // as
    // Mul[] visibility() defaull []);
}
// Problemet er den constant... Vi bliver ogsaa noedt til at have den her...
// Hvis den skal bruges standalone
// Og ahh det er aandsvagt at skulle specificere den 2 steder...
// Saa syntes vi skal have en exported=true paa Provide


// Extensions cannot export services...