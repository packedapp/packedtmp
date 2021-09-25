package app.packed.service;

public @interface Export {
    Class<?> as() default FromMethodSignature.class;

    ProvisionMode[] mode() default {}; // Use value from @Provide, if no @Provide and empty = On_DEMAND, otherwise use single value

    // If empty it takes the value of @Provide
    // Used standalone either this single value or defaults to Prototype
    // Doo<int, String>
    Class<?>[] parameters() default {};
}

class FromMethodSignature {}

// // Problemet er den constant... Vi bliver ogsaa noedt til at have den her...
// Hvis den skal bruges standalone
// Og ahh det er aandsvagt at skulle specificere den 2 steder...
// Saa syntes vi skal have en exported=true paa Provide

// Problemet er saa at man ikke kun kan exportere...
// med mindre man bruger mode = ProvideOnly, ExportOnly, Both 
// Hvilket virker langt mere kompliceret


// Extensions cannot export services...