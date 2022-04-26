package app.packed.bean;

import java.util.Optional;

import app.packed.application.ApplicationMirror;
import app.packed.component.ComponentMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.operation.lifecycle.BeanFactoryOperationMirror;
import packed.internal.bean.BeanSetup.BuildTimeBeanMirror;

/**
 * A mirror of a single bean.
 * <p>
 * Instances of this class is typically via {@link ApplicationMirror} or {@link ContainerMirror}.
 */
public sealed interface BeanMirror extends ComponentMirror permits BuildTimeBeanMirror {

    /**
     * Returns the type (class) of the bean.
     * <p>
     * If the bean does not have a proper class, for example, a functional bean. {@code void.class} is returned.
     * 
     * @return the type (class) of the bean.
     */
    Class<?> beanClass();

    /** {@return the bean's kind.} */
    BeanKind beanKind();

    /** {@return the container the bean belongs to. Is identical to #parent() which is always present for a bean.} */
    ContainerMirror container();
}

interface zandbox {

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    default Optional<BeanFactoryOperationMirror> factory() {
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
