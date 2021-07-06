package app.packed.extension.old;

import app.packed.base.NamespacePath;

/**
 * A configuration object for an ExtensionBean.
 * <p>
 * An instance of this class is returned when installing an Extensor via
 * 
 * ExtensionContext or
 * 
 * Extension.
 * 
 */
// .ServiceExtension
// .ServiceExtension
// .ServiceExtension
// .ServiceExtension
public final class ExtensionBeanConfiguration {

    // Bind er lokalt inject, provide er container scope

    // Altsaa hvorfor kan vi ikke bare gemme det i selve extension'en???
    // Og saa hive det ud derfra??
    public <T> ExtensionBeanConfiguration bindInstance(Class<T> key, T instance) {
        return this;
    }

    public ExtensionBeanConfiguration bindInstance(Object o) {
        return this;
    }

    public ExtensionBeanConfiguration named(String name) {
        return this;
    }

    public NamespacePath path() {
        throw new UnsupportedOperationException();
    }

    // Nu skal de sgu nok hedde .ServiceExtension alligevel paa runtime
}
///// Application Level...
/// Maaske er det bare en abstract klasse man extender
/// Det eneste man kan bruge der er application level context
/// Men den maa kunne faa parents injected...

// Alternativ installere den via en static something...
// Eneste problem er de der hooks