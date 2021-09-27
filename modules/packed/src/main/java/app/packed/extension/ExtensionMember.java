package app.packed.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * {@link ExtensionSupport} implementations
 * 
 * {@link ExtensionMirror} implementations
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ExtensionMember {
    
    /** {@return the extension the annotated class is a member of.} */
    Class<? extends Extension> value();
}

//Vil sige den er god til at dokumentere hvem der er hvem. Men vi behoever jo egentlig ikke en faelles klasse
//ExtensionWirelet -> Har vi brug for at vide hvilken extension vi skal brokke os over ikke eksistere
//Extensor -> Har vi brug for at vide hvilke extensors kan finde hinanden
//ExtensionBean hvem hoerer du til (hvis autoinstallable), eller den hoerer vel bare til den
//extension et hook er en del af, eller den extension der installere den
