package app.packed.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to indicate that the annotated type belongs to a particular {@link Extension}.
 * <p>
 * The following types must use this annotation to indicate what extension they are part of:
 * <ul>
 * <li>{@link ExtensionMirror} subclasses. Packed uses this information to figure out what extension to call
 * {@link Extension#mirror()} on in order to get an instance of the particular extension mirror.</li>
 * <li>{@link ExtensionSupport} subclasses. Packed uses this information to instantiate the support class with the right
 * type of extension.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExtensionMember {

    /**
     * Returns the extension the annotated class is a member of. The annotated class must be located in the same module as
     * the extension defined in this value. Otherwise an {@link InternalExtensionException} will be thrown at runtime.
     */
    Class<? extends Extension> value();
}

//Vil sige den er god til at dokumentere hvem der er hvem. Men vi behoever jo egentlig ikke en faelles klasse
//ExtensionWirelet -> Har vi brug for at vide hvilken extension vi skal brokke os over ikke eksistere
//Extensor -> Har vi brug for at vide hvilke extensors kan finde hinanden
//ExtensionBean hvem hoerer du til (hvis autoinstallable), eller den hoerer vel bare til den
//extension et hook er en del af, eller den extension der installere den
