package app.packed.extension;

/**
 * Extension points are the main mechanism by which an extension can use another extension. Developers that are not
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
 * @see ExtensionPointContext
 */
public abstract class ExtensionPoint<E extends Extension<E>> {}
