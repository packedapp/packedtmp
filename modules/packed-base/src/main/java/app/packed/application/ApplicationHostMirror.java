package app.packed.application;

import app.packed.component.ComponentMirror;
import app.packed.mirror.Mirror;
import app.packed.mirror.SetView;

/**
 * A mirror of an application host.
 * 
 */

// Kan vi have en Host med forskellige Applications typer for en host????
// Jeg har svaert ved at se det... Saa maa man lave forskellige hosts...
// I 9/10 af tilfaeldene er de vel ogsaa void...
public interface ApplicationHostMirror extends Mirror {

    /** {@return the application the host is a part of.} */
    ApplicationMirror application();

    /**
     * Returns the component that represents the application host.
     * <p>
     * An application host is always represented as a component in the application that is doing the actual hosting.
     * 
     * @return the component that represents the application host
     */
    ComponentMirror component();

    // Set<VersionableHost> multiHosts(); Eller hvad vi nu vil kalde dem...

    /** @return {a collection of all applications that are current deployment on the host.} */
    SetView<ApplicationMirror> installations();

    // All applications that are not versionable
    SetView<ApplicationMirror> nonVersionable();

    SetView<VersionableApplicationMirror> versionable();
}
// Mirrors never now anything about instances...

// Vi kunne have en DeployedApplicationMirror.. Med lidt ekstra metoder... But I think not
