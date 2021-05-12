package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentAttributes;
import app.packed.component.Wirelet;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.attribute.DefaultAttributeMap;

public final class SourcedComponentSetup extends ComponentSetup {

    /** The class source setup if this component has a class source, otherwise null. */
    public final ClassSourceSetup source;

    public SourcedComponentSetup(ApplicationSetup application, RealmSetup realm, ClassComponentDriver<?> driver,
            @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(application, realm, driver, parent, wirelets);
        this.source = new ClassSourceSetup(this, driver.binding);

        // Set the name of the component if it have not already been set using a wirelet
        if (name == null) {
            initializeNameWithPrefix(source.hooks.simpleName());
        }
    }

    @Override
    protected void attributesAdd(DefaultAttributeMap dam) {
        dam.addValue(ComponentAttributes.SOURCE_CLASS, source.hooks.clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) container.injection.getServiceManagerOrCreate().exports().export(source.service);
    }

    public void sourceProvide() {
        realm.checkOpen();
        source.provide(this);
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        realm.checkOpen();
        source.provide(this).as(key);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }
}
