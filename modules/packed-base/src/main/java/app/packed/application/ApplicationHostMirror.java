package app.packed.application;

import app.packed.component.ComponentMirror;
import app.packed.mirror.SetView;

/**
 * A mirror of an application host.
 */
public interface ApplicationHostMirror extends ComponentMirror {

    /**
     * Dynamic hosts allow new code, static does not..
     * @return sd
     */
    boolean isDynamic();
    
    // Set<VersionableHost> multiHosts(); Eller hvad vi nu vil kalde dem...

    /** @return {a collection of all applications that are installed on the host.} */
    SetView<ApplicationMirror> installations(); // applications

    // All applications that are not versionable
    SetView<ApplicationMirror> nonVersionable();

    SetView<VersionableApplicationMirror> versionable();
}

// Mirrors never k now anything about instances...

// Vi kunne have en InstalledApplicationMirror.. Med lidt ekstra metoder... But I think not
// Nej
///** {@return the application the host is installed in. This is a shortcut for {@code component().application().} */
//@Override
//default ApplicationMirror application() {
//  return component().application();
//}
//
///**
//* Returns the component that represents the application host.
//* <p>
//* An application host is always represented as a component in the application that is doing the actual hosting.
//* 
//* @return the component that represents the application host
//*/
//ComponentMirror component();
