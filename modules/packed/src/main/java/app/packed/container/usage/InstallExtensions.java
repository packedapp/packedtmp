package app.packed.container.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.container.AssemblySetup;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.Extension;
import app.packed.inject.service.ServiceExtension;

@AssemblySetup(InstallExtensionsX.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InstallExtensions {
    Class<? extends Extension<?>>[] value();
}

record InstallExtensionsX(InstallExtensions pc) implements AssemblySetup.Processor {

    @Override
    public void beforeBuild(ContainerConfiguration configuration) {
        for (Class<? extends Extension<?>> c : pc.value()) {
            configuration.use(c);
        }
    }
}

@InstallExtensions(ServiceExtension.class)
abstract class MyB extends BaseAssembly {}