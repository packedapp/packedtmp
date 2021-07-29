package app.packed.extension.sandbox;

import app.packed.extension.Extension;
import app.packed.extension.OldExtensionMember;

// Ideen er at en extensor (nuvaerende extensor->Extension Bean) bliver lavet per
// injection site... Det er som en slags prototype service.
// Men den er ikke registreret nogen steder
public non-sealed interface Extensor<E extends Extension> extends OldExtensionMember<E> {

}
