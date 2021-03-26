package packed.internal.component.source;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.ComponentAttributes;
import app.packed.component.Wirelet;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.BuildSetup;
import packed.internal.attribute.DefaultAttributeMap;
import packed.internal.component.WireableComponentSetup;
import packed.internal.component.ComponentSetup;
import packed.internal.component.RealmSetup;
import packed.internal.component.SourcedComponentDriver;

public final class SourceComponentSetup extends WireableComponentSetup {

    /** The class source setup if this component has a class source, otherwise null. */
    public final ClassSourceSetup source;

    public SourceComponentSetup(BuildSetup build, RealmSetup realm, SourcedComponentDriver<?> driver, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(build, realm, driver, parent, wirelets);
        this.source = new ClassSourceSetup(this, driver);

        // Set a default name if up default name
        if (name == null) {
            setName0(null);
        }
    }

    @Override
    protected void addAttributes(DefaultAttributeMap dam) {
        dam.addValue(ComponentAttributes.SOURCE_CLASS, source.model.type);
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) container.getServiceManagerOrCreate().exports().export(source.service);
    }

    public void sourceProvide() {
        checkConfigurable();
        source.provide(this);
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        checkConfigurable();
        source.provide(this).as(key);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }
}
