package app.packed.build.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.build.ApplyBuildHook;
import app.packed.bundle.BaseBundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.sandbox.BundleHook;
import app.packed.extension.Extension;
import app.packed.service.ServiceExtension;

@ApplyBuildHook(InstallExtensionsX.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface InstallExtensions {
    Class<? extends Extension>[] value();
}

record InstallExtensionsX(InstallExtensions pc) implements BundleHook {

    @Override
    public void beforeBuild(BundleConfiguration configuration) {
        for (Class<? extends Extension> c : pc.value()) {
            configuration.use(c);
        }
    }
}

@InstallExtensions(ServiceExtension.class)
abstract class MyB extends BaseBundle {}