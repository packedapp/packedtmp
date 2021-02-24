package app.packed.inject;

//IDK

// Det gode ved at have en seperat export er

// Vi kan lave en custom type som returnere noget andet
// Vi kan exportere 
public @interface Export {
    Class<?>[] as() default {};
}
