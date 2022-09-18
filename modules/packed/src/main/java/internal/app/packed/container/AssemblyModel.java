package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

import app.packed.application.BuildException;
import app.packed.container.Assembly;
import app.packed.container.ContainerConfiguration;
import app.packed.container.ContainerHook;
import internal.app.packed.util.ThrowableUtil;

/** A model of an {@link Assembly}. */
public final /* primitive */ class AssemblyModel {

    /** Cached models of assembly classes. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<ContainerHook.Processor> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof ContainerHook h) {
                    for (Class<? extends ContainerHook.Processor> b : h.value()) {
                        if (ContainerHook.Processor.class.isAssignableFrom(b)) {
                            MethodHandle constructor;
                            // TODO fix visibility
                            // Maybe common findConstructorMethod
                            try {
                                constructor = MethodHandles.lookup().findConstructor(b, MethodType.methodType(void.class));
                            } catch (NoSuchMethodException e) {
                                throw new BuildException("A container hook must provide an empty constructor, hook = " + h, e);
                            } catch (IllegalAccessException e) {
                                throw new BuildException("Can't see it sorry, hook = " + h, e);
                            }
                            constructor = constructor.asType(MethodType.methodType(ContainerHook.Processor.class));

                            ContainerHook.Processor instance;
                            try {
                                instance = (ContainerHook.Processor) constructor.invokeExact();
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            return new AssemblyModel(hooks.toArray(s -> new ContainerHook.Processor[s]));
        }
    };

    /** Any hooks that have been specified on the assembly. */
    private final ContainerHook.Processor[] hooks;

    private AssemblyModel(ContainerHook.Processor[] hooks) {
        this.hooks = requireNonNull(hooks);
    }

    public void postBuild(ContainerConfiguration configuration) {
        for (ContainerHook.Processor h : hooks) {
            h.afterBuild(configuration);
        }
    }

    public void preBuild(ContainerConfiguration configuration) {
        for (ContainerHook.Processor h : hooks) {
            h.beforeBuild(configuration);
        }
    }

    public static AssemblyModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}

class ContainerBuildHookModel {
    final ContainerHook.Processor hook;

    ContainerBuildHookModel(Builder builder) {
        this.hook = null;
    }

    static class Builder {

    }
}
