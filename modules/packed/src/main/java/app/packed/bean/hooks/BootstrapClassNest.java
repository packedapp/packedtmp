package app.packed.bean.hooks;

import java.util.List;

// Den er midlertidig!!! Dvs. man kan ikke faa den injected taenker jeg...
// Aaahh den passer ikke super godt med Variable/Parameter hook...
/**
 *
 * Nest instances are not retained after a class has been bootstrapped.
 */
public abstract class BootstrapClassNest {

    Object builder;
    
    public final List<OldBeanClass> classes() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Returns a list of methods in this nest.
     * 
     * @return a list of all methods in this nest
     * @see OldBeanField#manageBy(Class)
     * @see OldBeanMethod#nestWith(Class)
     */
    public final List<OldBeanMethod> methods() {
        return methods(OldBeanMethod.class);
    }

    public final <T extends OldBeanMethod> List<T> methods(Class<T> type) {
        throw new UnsupportedOperationException();
    }
    
    // Invoked just before the class is "leaving" bootstrap
    protected void onBootstrapped() {}
}
