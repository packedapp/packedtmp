package app.packed.bean;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import app.packed.application.ApplicationMirror;
import app.packed.application.ComponentMirror;
import app.packed.base.NamespacePath;
import app.packed.base.Nullable;
import app.packed.container.AssemblyMirror;
import app.packed.container.ContainerMirror;
import app.packed.container.Extension;
import app.packed.container.Realm;
import app.packed.lifetime.LifetimeMirror;
import app.packed.operation.OperationMirror;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.Mirror;
import internal.app.packed.operation.OperationSetup;

/**
 * A mirror of a bean.
 * <p>
 * Instances of this class is typically obtained from calls to {@link ApplicationMirror} or {@link ContainerMirror}.
 */
public non-sealed class BeanMirror implements ComponentMirror, Mirror {

    /**
     * The internal configuration of the bean we are mirroring. Is initially null but populated via
     * {@link #initialize(BeanSetup)}.
     */
    @Nullable
    private BeanSetup bean;

    /**
     * Create a new bean mirror.
     * <p>
     * Subclasses should have a single package-protected constructor.
     */
    public BeanMirror() {}

    /**
     * {@return the internal configuration of the bean we are mirroring.}
     * 
     * @throws IllegalStateException
     *             if {@link #initialize(BeanSetup)} has not been called previously.
     */
    private BeanSetup bean() {
        BeanSetup b = bean;
        if (b == null) {
            throw new IllegalStateException(
                    "Either this method has been called from the constructor of the mirror. Or the mirror has not yet been initialized by the runtime.");
        }
        return b;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(Object other) {
        return this == other || other instanceof BeanMirror m && bean() == m.bean();
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {
        return bean().hashCode();
    }

    /**
     * Invoked by the runtime with the internal configuration of the bean to mirror.
     * 
     * @param bean
     *            the internal configuration of the bean to mirror
     */
    final void initialize(BeanSetup bean) {
        if (this.bean != null) {
            throw new IllegalStateException("This mirror has already been initialized.");
        }
        this.bean = bean;
    }

    /** {@return the owner of the component.} */
    public Realm owner() {
        return bean().realm.realm();
    }

    /** {@inheritDoc} */
    @Override
    public ApplicationMirror application() {
        return bean().application.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public AssemblyMirror assembly() {
        return bean().userRealm.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public Stream<ComponentMirror> stream() {
        return Stream.of(this);
    }

    /** {@inheritDoc} */
    @Override
    public int depth() {
        return bean().depth;
    }

    /** {@inheritDoc} */
    @Override
    public LifetimeMirror lifetime() {
        return bean().lifetime.mirror();
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return bean().name;
    }

    public Collection<LifetimeMirror> managesLifetimes() {
        // Find LifetimeOperations->Unique on Lifetime
        throw new UnsupportedOperationException();
    }

    public NamespacePath path() {
        return bean().path();
    }

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
    public Class<? extends Extension<?>> operator() {
        return bean.operator();
    }

    public Stream<OperationMirror> operations() {
        return bean().operations.stream().map(OperationSetup::mirror);
    }

    /**
     * Returns the type (class) of the bean.
     * <p>
     * Beans that do not have a proper class, for example, a functional bean. Will have {@code void.class} as their bean
     * class.
     * 
     * @return the type (class) of the bean.
     */
    public Class<?> beanClass() {
        return bean().builder.beanClass();
    }

    /** {@return the bean's kind.} */
    public BeanKind beanKind() {
        return bean.builder.beanKind();
    }

    /** {@return the container the bean belongs to. Is identical to #parent() which is never optional for a bean.} */
    public ContainerMirror container() {
        return bean.parent.mirror();
    }
}

interface SSandbox {

    // @SuppressWarnings({ "unchecked", "rawtypes" })
    default Optional<Object /* BeanFactoryOperationMirror */> factory() {
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
