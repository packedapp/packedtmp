package app.packed.build.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.build.ApplyBuildHook;
import app.packed.build.BuildException;
import app.packed.bundle.BundleMirror;
import app.packed.bundle.sandbox.BundleHook;

@ApplyBuildHook(RandomProcX.class)
@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MaximumComponentContainer {
    int maxComponents();
}

record RandomProcX(MaximumComponentContainer pc) implements BundleHook {

    @Override
    public void onCompleted(BundleMirror mirror) {
        if (mirror.components().count() > pc.maxComponents()) {
            throw new BuildException("Cannot define a container with more than " + pc.maxComponents() + " components in a single container");
        }
    }
}
