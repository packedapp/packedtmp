package app.packed.inject;

//IDK

// Det gode ved at have en seperat export er

// Vi kan lave en custom type som returnere noget andet
// Vi kan exportere 
public @interface Export {
    Class<?>[] as() default {};
}

// Problemet er den constant... Vi bliver ogsaa noedt til at have den her...
// Hvis den skal bruges standalone
// Og ahh det er aandsvagt at skulle specificere den 2 steder...
// Saa syntes vi skal have en exported=true paa Provide


// Extensions cannot export services...