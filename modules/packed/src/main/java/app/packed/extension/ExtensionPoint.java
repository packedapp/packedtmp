package app.packed.extension;

import app.packed.component.Realm;
import packed.internal.container.PackedExtensionPointUseSite;

/**
 * Extension points are the main mechanism by which an extension can use other extensions. Developers that are not
 * creating their own extensions will likely never have to deal with these type of classes.
 * <p>
 * 
 * <p>
 * Hvorfor eksistere disse support klasser
 * 
 * Vi vil gerne provide andre services til extensions istedet for application developers.
 * 
 * The application developers is in control not the extension developers
 * <p>
 * Extension support classes are typically (although not required) defined as a primitive class. Taking the extension
 * that it is a member of as a parameter in the constructor.
 * 
 * Make example... Maybe usin
 * <p>
 * 
 * <p>
 * Potentially also taking the requesting extension class as a parameter.
 * 
 * An extension There are no annotations that make sense for this class
 * 
 * {@code Class<? extends Extension<?>>} which is the
 * 
 * <p>
 * A new extension point instance is automatically created by the runtime when {@link Extension#use(Class)} or
 * {@link ExtensionConfiguration#use(Class)} is called. These instances are <strong>never</strong> cached but created
 * every time it is requested.
 * 
 * @see Extension#use(Class)
 * @see ExtensionConfiguration#use(Class)
 * @see UseSite
 */
public abstract class ExtensionPoint<E extends Extension<E>> {

    private PackedExtensionPointUseSite useSite;

    @SuppressWarnings("unchecked")
    protected final E extension() {
        return (E) usesite().extension().instance();
    }

    /**
     * <p>
     * ExtensionSetup is exposed as {@link ExtensionConfiguration} via {@link #configuration()}.
     * 
     * @return
     */
    private final PackedExtensionPointUseSite usesite() {
        PackedExtensionPointUseSite c = useSite;
        if (c == null) {
            throw new IllegalStateException("This operation cannot be invoked from the constructor of the extension point.");
        }
        return c;
    }

    protected final UseSite useSite() {
        return usesite();
    }

    /**
     * A context object that can be injected into subclasses of {@link ExtensionPoint}.
     */
    // Svaert at have ExtensionContext (paa runtime) samtidig med denne

    // Inner class: UseSite
    //// Er lidt underlig maaske med UseSite hvis man tager den som parameter
    //// Men vil ikke mere hvor man skal tage et ExtensionPointContext???
    public sealed interface UseSite permits PackedExtensionPointUseSite {

        /**
         * 
         * @see Extension#checkConfigurable()
         */
        void checkConfigurable();

        default void checkInSameContainerAs(Extension<?> extension) {
            // Maaske vi skal lave nogle checks saa man ikke bare kan bruge den hvor man har lyst.
            // Men at vi binder den til en container...

            // IDK
            // ExtensionSupportUSer???
        }

        Class<? extends Extension<?>> extensionType();

        Realm realm();
    }
    //
    //// checkExtendable...
    /// **
    // * Checks that the new extensions can be added to the container in which this extension is registered.
    // *
    // * @see #onAssemblyClose()
    // */
    //// Altsaa det er jo primaert taenkt paa at sige at denne extension operation kan ikke blive invokeret
    //// af brugeren med mindre XYZ...
    //// Det er jo ikke selve extension der ved en fejl kommer til at kalde operationen...
    // protected final void checkExtensionConfigurable(Class<? extends Extension<?>> extensionType) {
//        configuration().checkExtensionConfigurable(extensionType);
    // }
}
