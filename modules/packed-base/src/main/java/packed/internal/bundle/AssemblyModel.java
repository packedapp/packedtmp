package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.base.Key;
import app.packed.build.ApplyBuildHook;
import app.packed.build.BuildHook;
import app.packed.bundle.Bundle;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.sandbox.AssemblyBuildHook;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

/**
 * A model of an {@link Bundle}.
 */
public final /* primitive */ class AssemblyModel {
    private final AssemblyBuildHook[] hooks;

    private AssemblyModel(AssemblyBuildHook[] hooks) {
        this.hooks = requireNonNull(hooks);
    }

    /** Cached models. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<AssemblyBuildHook> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof ApplyBuildHook h) {
                    for (Class<? extends BuildHook> b : h.value()) {
                        if (AssemblyBuildHook.class.isAssignableFrom(b)) {
                            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), b, Class.class);
                            builder.provide(new Key<Class<? extends Bundle<?>>>() {}).adaptArgument(0);
                            // If it is only ServiceExtension that ends up using it lets just dump it and have a single cast
                            // builder.provideHidden(ExtensionSetup.class).adaptArgument(0);
                            // Den den skal nok vaere lidt andet end hidden. Kunne kunne klare Optional osv
                            // MethodHandle mh =
                            // ExtensionSetup.MH_INJECT_PARENT.asType(ExtensionSetup.MH_INJECT_PARENT.type().changeReturnType(extensionClass));
                            // builder.provideHidden(extensionClass).invokeExact(mh, 0);

                            // Find a method handle for the extension's constructor

                            MethodHandle constructor = builder.findConstructor(AssemblyBuildHook.class, m -> new InternalExtensionException(m));

                            AssemblyBuildHook instance;
                            try {
                                instance = (AssemblyBuildHook) constructor.invokeExact(type);
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            return new AssemblyModel(hooks.toArray(s -> new AssemblyBuildHook[s]));
        }
    };

    public void preBuild(BundleConfiguration configuration) {
        for (AssemblyBuildHook h : hooks) {
            h.onPreBuild(configuration);
        }
    }

    public void postBuild(BundleConfiguration configuration) {
        for (AssemblyBuildHook h : hooks) {
            h.onPostBuild(configuration);;
        }
    }

    public static AssemblyModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}

class ContainerBuildHookModel {
    final AssemblyBuildHook hook;

    ContainerBuildHookModel(Builder builder) {
        this.hook = null;
    }

    static class Builder {

    }
}
