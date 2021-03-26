package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentConfigurationContext;
import app.packed.component.ComponentModifier;
import app.packed.component.Wirelet;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.BuildSetup;
import packed.internal.component.source.ClassSourceSetup;

public class BaseComponentSetup extends ComponentSetup implements ComponentConfigurationContext {
    /** The class source setup if this component has a class source, otherwise null. */
    @Nullable
    public final ClassSourceSetup source;
    
    public BaseComponentSetup(BuildSetup build, RealmSetup realm, PackedComponentDriver<?> driver, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
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

    public void sourceProvide() {
        checkConfigurable();
        checkHasSource();
        source.provide(this);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        checkHasSource();
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) memberOfContainer.getServiceManagerOrCreate().exports().export(source.service);
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        if (source == null) {
            throw new UnsupportedOperationException();
        }
        source.provide(this).as(key);
    }

}
