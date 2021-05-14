package app.packed.application.host;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

// In packed an application host is an entity(component?) that can host applications.

// Manage application instances (and images)
// Control their lifecycle
// Provide services/features (form the host)
// Control intra-application communication

// Den er ligesom et service registry...
// Informational only...
public /* sealed */ interface ApplicationHost extends Iterable<ApplicationGuest> {

    Map<String, ApplicationGuest> asMap();

    Optional<ApplicationGuest> find(String name);

    Stream<ApplicationGuest> guests();

    /** {@return the number of application instances that are managed by this host.} */
    int size();

    // forEach...() <--- hvad faar man? ApplicationGuest?

    /** {@return a host without any guests} */
    static ApplicationHost of() {
        throw new UnsupportedOperationException();
    }

    // Returns the platform application host
    // there is only a single platform application host..
  
    /**
     * @return
     */
    /// maaske er de alle registreret under
    // platform/ddd???? 
    // Og saa er platform en registreret
    // Eller maaske har JFR bare dens egen host???
    // Maaske er det en ApplicationGroup istedet for...
    // Flere parents...
    
    // Eller ogsaa er det et immutable <String, Guest> map...
    
    static ApplicationHost platformHost() {
        // lazy initialized...
        // Cannot use while building an image...
        throw new UnsupportedOperationException();
    }
}
// PlatformApplication() {
// ApplicationHost() host(); //returns the platform host
//}

// Would be nice to capture what module installed a platform