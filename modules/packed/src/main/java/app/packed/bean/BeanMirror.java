package app.packed.bean;

import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.application.ComponentMirror;
import app.packed.application.Realm;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import internal.app.packed.bean.BeanSetup.BuildTimeBeanMirror;
import internal.app.packed.container.Mirror;

/**
 * A mirror of a bean.
 * <p>
 * Instances of this class is typically obtained from calls to {@link ApplicationMirror} or {@link ContainerMirror}.
 */
public sealed interface BeanMirror extends ComponentMirror, Mirror permits BuildTimeBeanMirror {


    /** {@return the owner of the component.} */
    Realm owner();

    /**
     * Returns any extension the bean's driver is part of. All drivers are either part of an extension. Or is a build in
     * drive
     * <p>
     * Another thing is extension member, which is slightly different.
     * 
     * @return any extension the bean's driver is part of
     */
    // Hvem ejer den bean driver, der er blevet brugt til at registrere bean'en...
    // Det er samtidig ogsaa den extension (if present) som evt. ville kunne instantiere den

    // Altsaa den giver jo ogsaa mening for en funktion. Ikke rigtig for en container dog
    // Eller en TreeBean (som jeg taenker aldrig kan registreres via en extension)
    // Saa maaske skal den flyttes ned paa component

    // Tror maaske den skal op paa ComponentMirror...
    // Ved ikke om vi kan definere end ContainerDriver for en extension???
    // Det primaere er vel injection
    // Er det i virkeligheden altid ownership???
    // Har vi tilfaelde hvor vi har en ikke-standard bean driver.
    // Hvor det ikke er extension'en der soerger for instantiering

    // RegisteredWith
    // DeclaredBy
    // Det er jo mere eller Realmen her

    // Giver den her super meget mening????
    /// fx @Get paa install(Foo.class) vs requestGet(Foo.class)
    /// Vil jo have forskllig registrant...
    /// Er nok mere relevant hvem der styre lifecyclen
    
    // Det er vel mere operator????

    
    // !!!! Den fungere jo ikke for containere???
    
    // var Optional<Class<? extends Extension<?>>> registrant
    // Giver strengt tagt kun mening paa beans nu..
    Class<? extends Extension<?>> operator();

    
    /**
     * Returns the type (class) of the bean.
     * <p>
     * Beans that do not have a proper class, for example, a functional bean. Will have {@code void.class} as their bean
     * class.
     * 
     * @return the type (class) of the bean.
     */
    Class<?> beanClass();

    /** {@return the bean's kind.} */
    BeanKind beanKind();

    /** {@return the container the bean belongs to. Is identical to #parent() which is never optional for a bean.} */
    ContainerMirror container();
}

interface SSandbox {

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    default Optional<Object /*BeanFactoryOperationMirror */> factory() {
        // return (Optional) operations().stream().filter(m ->
        // BeanFactoryOperationMirror.class.isAssignableFrom(m.getClass())).findAny();
        // Kunne man forstille sig at en bean havde 2 constructors??
        // Som man valgte af paa runtime????
        throw new UnsupportedOperationException();
    }

    default Class<? extends Extension<?>> installedVia() {
        // The extension that performed the actual installation of the bean
        // Den burde ligge paa Component???
        // Nah
        return BeanExtension.class;
    }

    // No instances, Instantiable, ConstantInstance
    // Scope-> BuildConstant, RuntimeConstant, Prototype...

    default Class<?> sourceType() {
        // returns Factory, Class or Object???
        // Maaske en enum istedet
        throw new UnsupportedOperationException();
    }
}
