package app.packed.container.usage;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import app.packed.build.BuildException;
import app.packed.container.AssemblySetup;
import app.packed.container.BaseAssembly;
import app.packed.container.ContainerMirror;

// @MetaAnnotation(AssemblyHook.class)
@AssemblySetup(RandomProcX.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface MaximumComponentContainer {
    int maxComponents();
}

@MaximumComponentContainer(maxComponents = 123)
class MyAss extends BaseAssembly {

    @Override
    protected void build() {
        // TODO Auto-generated method stub
    }
}

record RandomProcX(MaximumComponentContainer pc) implements AssemblySetup.Processor {

    @Override
    public void onCompleted(ContainerMirror mirror) {
        if (mirror.components().count() > pc.maxComponents()) {
            throw new BuildException("Cannot define a container with more than " + pc.maxComponents() + " components in a single container");
        }
    }
}