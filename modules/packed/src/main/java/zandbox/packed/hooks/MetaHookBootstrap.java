package zandbox.packed.hooks;

// @MetaHookBootstrap vs metode...
// Jeg tror maaske jeg er fortaller for en annotering... // Teknisk set kan vi saa brug dem andet steds...

// Alternativt skal vi have en metode paa bootstrap der markere
public @interface MetaHookBootstrap {}

// Originalt havde vi nogle $metoder i bootstrap for hver speciel type hvor man angav en andet bootstrap class for en specifik specialisering
// Det bliver dog lidt sjusket at loade det paa den... Skal lave alle mulige checks...
/// Saa kom der multiple bootstrap klasser
