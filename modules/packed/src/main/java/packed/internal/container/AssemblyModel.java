package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.base.Key;
import app.packed.container.Assembly;
import app.packed.container.AssemblySetup;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

/** A model of an {@link Assembly}. */
public final /* primitive */ class AssemblyModel {
    
    private final AssemblySetup.Processor[] hooks;

    private AssemblyModel(AssemblySetup.Processor[] hooks) {
        this.hooks = requireNonNull(hooks);
    }

    /** Cached models. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<AssemblySetup.Processor> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof AssemblySetup h) {
                    for (Class<? extends AssemblySetup.Processor> b : h.value()) {
                        if (AssemblySetup.Processor.class.isAssignableFrom(b)) {
                            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), b, Class.class);
                            builder.provide(new Key<Class<? extends Assembly >>() {}).adaptArgument(0);
                            // If it is only ServiceExtension that ends up using it lets just dump it and have a single cast
                            // builder.provideHidden(ExtensionSetup.class).adaptArgument(0);
                            // Den den skal nok vaere lidt andet end hidden. Kunne kunne klare Optional osv
                            // MethodHandle mh =
                            // ExtensionSetup.MH_INJECT_PARENT.asType(ExtensionSetup.MH_INJECT_PARENT.type().changeReturnType(extensionClass));
                            // builder.provideHidden(extensionClass).invokeExact(mh, 0);

                            // Find a method handle for the extension's constructor

                            MethodHandle constructor = builder.findConstructor(AssemblySetup.Processor.class, m -> new InternalExtensionException(m));

                            AssemblySetup.Processor instance;
                            try {
                                instance = (AssemblySetup.Processor) constructor.invokeExact(type);
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            return new AssemblyModel(hooks.toArray(s -> new AssemblySetup.Processor[s]));
        }
    };

    public void preBuild(ContainerConfiguration configuration) {
        for (AssemblySetup.Processor h : hooks) {
            h.beforeBuild(configuration);
        }
    }

    public void postBuild(ContainerConfiguration configuration) {
        for (AssemblySetup.Processor h : hooks) {
            h.afterBuild(configuration);
        }
    }

    public static AssemblyModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}

class ContainerBuildHookModel {
    final AssemblySetup.Processor hook;

    ContainerBuildHookModel(Builder builder) {
        this.hook = null;
    }

    static class Builder {

    }
}