package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.container.ContainerAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.Extension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.BuildSetup;
import packed.internal.component.source.ClassSourceSetup;

public class SourceComponentSetup extends ComponentSetup implements ComponentConfigurationContext {
    
    /** The class source setup if this component has a class source, otherwise null. */
    @Nullable
    public final ClassSourceSetup source;

    public SourceComponentSetup(BuildSetup build, RealmSetup realm, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(build, realm, driver, parent, wirelets);

        // Setup component sources
        if (driver.modifiers().isSource()) {
            this.source = new ClassSourceSetup(this, (SourcedComponentDriver<?>) driver);
        } else {
            this.source = null;
        }

        // Set a default name if up default name
        if (name == null) {
            setName0(null);
        }
    }

    /** Checks that this component has a source. */
    private void checkHasSource() {
        if (source == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".SOURCE modifier set");
        }
    }

    /** Checks that this component has a source. */
    private void checkIsContainer() {
        if (container == null) {
            throw new UnsupportedOperationException(
                    "This method can only be called component that has the " + ComponentModifier.class.getSimpleName() + ".CONTAINER modifier set");
        }
    }

    /**
     * Returns an unmodifiable view of the extensions that are currently in use.
     * 
     * @return an unmodifiable view of the extensions that are currently in use
     * 
     * @see ContainerAssembly#extensions()
     */
    // Maybe it is just an Attribute.. component.with(Extension.USED_EXTENSIONS)
    // for assembly components. Makes sense because we would need for interating
    // through the build
    public Set<Class<? extends Extension>> containerExtensions() {
        checkIsContainer();
        return container.extensionView();
    }

    /**
     * @param <T>
     * @param extensionClass
     * @return the extension
     * @throws UnsupportedOperationException
     *             if the underlying component is not a container
     * @see ContainerConfiguration#use(Class)
     */
    public <T extends Extension> T containerUse(Class<T> extensionClass) {
        checkIsContainer();
        return container.useExtension(extensionClass);
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) memberOfContainer.getServiceManagerOrCreate().exports().export(source.service);
    }

    public void sourceProvide() {
        checkConfigurable();
        checkHasSource();
        source.provide(this);
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        if (source == null) {
            throw new UnsupportedOperationException();
        }
        source.provide(this).as(key);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        checkHasSource();
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }

}
