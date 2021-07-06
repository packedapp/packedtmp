package app.packed.component;

import java.util.Optional;

// Tjahhh... Hmmm.. Mhmmmm.... IDK about this....

// Vi kan have nogle metoder paa Wirelet som man kan overskrive
// for at give flere informationer...

/**
 * A mirror of a wirelet.
 * <p>
 */
// Available from ApplicationMirror, ContainerMirror, ComponentMirror, ApplicationDriverMirror
interface WireletMirror {

    // isBuild(), isImage() har vi noget scope???

    String specifiedVia(); // Launch, Driver, ... Build.. I think Build takes precedense over launch

    // Der har vi issuen omkring Packed, Extension or user..
    Optional<?> extension();

    boolean isUserWirelet();
}

//Maaske har man et default scope som hedder
//Application <- hvis applications driver
//Container <- f.eks. linke container
class WireletScope {

}


// Svaert at argumentere imod at kunne koere denne paa runtime

// foo(ContainerMirror cm) {
//   cm.wirelets.print();
// }