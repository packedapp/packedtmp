package app.packed.container;

import app.packed.mirror.Mirror;

// Altsaa skal det ikke vaere en abstract klasse???
// Kan ikke forstille mig vi gemmer den her paa runtime.

// Kan bare sige der skal vaere en mirror() metode paa en Extension subclass
// Hvis der er det knytter vi dem sammen

// Vi vil gerne vaere serializable til gengaeld taenker jeg

// Eller ogsaa bare FooExtension.mirrorOf() og
// Mirror.of(FooExtension)
public interface ExtensionMirror<T extends Extension> extends Mirror {}
