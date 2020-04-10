package packed.internal.service.build.service;

import java.util.List;

import app.packed.base.Nullable;
import app.packed.config.ConfigSite;
import app.packed.service.ServiceMode;
import packed.internal.inject.ServiceDependency;
import packed.internal.service.build.BuildEntry;
import packed.internal.service.build.ServiceExtensionInstantiationContext;
import packed.internal.service.build.ServiceExtensionNode;
import packed.internal.service.runtime.InjectorEntry;

public class CompositeBuildEntry<T> extends BuildEntry<T> {

    public CompositeBuildEntry(@Nullable ServiceExtensionNode serviceExtension, ConfigSite configSite, List<ServiceDependency> dependencies) {
        super(serviceExtension, configSite, dependencies);
    }

    @Override
    public boolean hasUnresolvedDependencies() {
        return !dependencies.isEmpty();
    }

    @Override
    public ServiceMode instantiationMode() {
        return ServiceMode.PROTOTYPE;
    }

    @Override
    protected InjectorEntry<T> newRuntimeNode(ServiceExtensionInstantiationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean requiresPrototypeRequest() {
        return false;
    }
}
