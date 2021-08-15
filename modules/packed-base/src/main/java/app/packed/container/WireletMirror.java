package app.packed.container;

import java.util.Optional;

import app.packed.component.Operator;

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
    // Maaske er der bare ikke mirrors paa wirelets.
    // Eller maaske er der bare ikke mirrors paa internal wirelets
    Optional<Operator> registrant();

    boolean isUserWirelet();
}

//Maaske har man et default scope som hedder
//Application <- hvis applications driver
//Container <- f.eks. linke container


// Svaert at argumentere imod at kunne koere denne paa runtime

// foo(ContainerMirror cm) {
//   cm.wirelets.print();
// }