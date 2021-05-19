package packed.internal.component;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.Set;

import app.packed.base.Key;
import app.packed.base.Nullable;
import app.packed.component.BeanMirror;
import app.packed.component.Wirelet;
import app.packed.container.Extension;
import app.packed.inject.sandbox.ExportedServiceConfiguration;
import packed.internal.application.ApplicationSetup;
import packed.internal.component.PackedComponentDriver.BeanComponentDriver;

/** The internal configuration of a bean.  */
public final class BeanSetup extends ComponentSetup {

    /** The class source setup if this component has a class source, otherwise null. */
    public final BeanSetupSupport source;

    BeanSetup(ApplicationSetup application, RealmSetup realm, BeanComponentDriver<?> driver, @Nullable ComponentSetup parent, Wirelet[] wirelets) {
        super(application, realm, driver, parent, wirelets);
        this.source = new BeanSetupSupport(this, driver, driver.binding);

        // Set the name of the component if it have not already been set using a wirelet
        if (name == null) {
            initializeNameWithPrefix(source.hooks.simpleName());
        }
    }

    /** {@inheritDoc} */
    @Override
    public BeanMirror mirror() {
        return new BuildTimeBeanMirror();
    }

    @SuppressWarnings("unchecked")
    public <T> ExportedServiceConfiguration<T> sourceExport() {
        sourceProvide();
        return (ExportedServiceConfiguration<T>) container.injection.getServiceManagerOrCreate().exports().export(source.service);
    }

    public void sourceProvide() {
        realm.checkOpen();
        source.provide();
    }

    public void sourceProvideAs(Key<?> key) {
        requireNonNull(key, "key is null");
        realm.checkOpen();
        source.provide().as(key);
    }

    public Optional<Key<?>> sourceProvideAsKey() {
        return source.service == null ? Optional.empty() : Optional.of(source.service.key());
    }

    /** A build-time bean mirror. */
    private final class BuildTimeBeanMirror extends ComponentSetup.BuildTimeComponentMirror implements BeanMirror {

        /** {@inheritDoc} */
        @Override
        public Class<?> beanType() {
            return source.hooks.clazz;
        }

        /** {@inheritDoc} */
        @Override
        public Optional<Class<? extends Extension>> driverExtension() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Set<?> hooks() {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public <T> Set<?> hooks(Class<T> hookType) {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public BeanMode mode() {
            throw new UnsupportedOperationException();
        }
    }
}
