package app.packed.extension;

// Auto injectable by any other bean...
// Singleton
// Install-on-Demand

/**
 * An extension bean is special type of bean that can only be installed by extensions.
 * <p>
 * Extension beans are the main way for cross container communication at runtime.
 * 
 * An extension bean may be inherited from a parent container (or other direct ancestor). In which case the parent's
 * instance will be used in the child's container instead of instantiating a new instance.
 * 
 * <ul>
 * <li>It is not possible to install more than a single instance of an extension bean in a container</li>
 * <li>Searchable/Inheritable has a hirachi</li>
 * <li>Autoactivated, an extension bean may be auto installed</li>
 * <li>Referenciable by bean hooks</li>
 * </ul>
 */
public abstract class ExtensionBean {
    // wirelets
}

// automatically installable if readable
// Skal have ExtensionMember ellers indgaar den jo ikke i extensionen's life cycle

// Settings disableHookAutoInstall()