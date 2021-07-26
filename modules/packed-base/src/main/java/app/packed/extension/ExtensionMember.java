package app.packed.extension;

import app.packed.extension.sandbox.Extensor;

// Vil sige den er god til at dokumentere hvem der er hvem. Men vi behoever jo egentlig ikke en faelles klasse

// ExtensionWirelet -> Har vi brug for at vide hvilken extension vi skal brokke os over ikke eksistere
// Extensor -> Har vi brug for at vide hvilke extensors kan finde hinanden
// ExtensionMirror -> Har vi brug for at vide hvilken extension vi skal spoerge om mirroret...
// ExtensionBean hvem hoerer du til (hvis autoinstallable), eller den hoerer vel bare til den
// extension et hook er en del af, eller den extension der installere den

@SuppressWarnings("rawtypes") // Eclipse being a ...
public sealed interface ExtensionMember<E extends Extension> permits Extension,ExtensionBean,Extensor {}

// ExtensionMirror vil vi helst have @MemberOf...
// Mest fordi vi ikke gider at brugerne skal skrive ExtensionMirror<?>... 