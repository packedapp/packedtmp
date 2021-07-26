package packed.internal.container;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.base.Key;
import app.packed.build.ApplyBuildHook;
import app.packed.build.BuildHook;
import app.packed.container.Assembly;
import app.packed.container.AssemblyHook;
import app.packed.container.ContainerConfiguration;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

public final /* primitive */ class AssemblyModel {
    private final AssemblyHook[] hooks;

    private AssemblyModel(AssemblyHook[] hooks) {
        this.hooks = requireNonNull(hooks);
    }

    /** Models of all subtensions. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<AssemblyHook> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof ApplyBuildHook h) {
                    for (Class<? extends BuildHook> b : h.value()) {
                        if (AssemblyHook.class.isAssignableFrom(b)) {
                            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), b, Class.class);
                            builder.provide(new Key<Class<? extends Assembly<?>>>() {}).adaptArgument(0);
                            // If it is only ServiceExtension that ends up using it lets just dump it and have a single cast
                            // builder.provideHidden(ExtensionSetup.class).adaptArgument(0);
                            // Den den skal nok vaere lidt andet end hidden. Kunne kunne klare Optional osv
                            // MethodHandle mh =
                            // ExtensionSetup.MH_INJECT_PARENT.asType(ExtensionSetup.MH_INJECT_PARENT.type().changeReturnType(extensionClass));
                            // builder.provideHidden(extensionClass).invokeExact(mh, 0);

                            // Find a method handle for the extension's constructor

                            MethodHandle constructor = builder.findConstructor(AssemblyHook.class, m -> new InternalExtensionException(m));

                            AssemblyHook instance;
                            try {
                                instance = (AssemblyHook) constructor.invokeExact(type);
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            return new AssemblyModel(hooks.toArray(s -> new AssemblyHook[s]));
        }
    };

    public void preBuild(ContainerConfiguration configuration) {
        for (AssemblyHook h : hooks) {
            h.onPreBuild(configuration);
        }
    }

    public void postBuild(ContainerConfiguration configuration) {
        for (AssemblyHook h : hooks) {
            h.onPostBuild(configuration);;
        }
    }

    public static AssemblyModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}

class ContainerBuildHookModel {
    final AssemblyHook hook;

    ContainerBuildHookModel(Builder builder) {
        this.hook = null;
    }

    static class Builder {

    }
}
