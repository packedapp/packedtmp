package packed.internal.bundle;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;

import app.packed.base.Key;
import app.packed.build.ApplyBuildHook;
import app.packed.build.BuildHook;
import app.packed.bundle.Assembly;
import app.packed.bundle.BundleConfiguration;
import app.packed.bundle.sandbox.BundleHook;
import app.packed.extension.InternalExtensionException;
import packed.internal.invoke.Infuser;
import packed.internal.util.ThrowableUtil;

/** A model of an {@link Assembly}. */
public final /* primitive */ class BundleModel {
    private final BundleHook[] hooks;

    private BundleModel(BundleHook[] hooks) {
        this.hooks = requireNonNull(hooks);
    }

    /** Cached models. */
    private final static ClassValue<BundleModel> MODELS = new ClassValue<>() {

        @Override
        protected BundleModel computeValue(Class<?> type) {
            ArrayList<BundleHook> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof ApplyBuildHook h) {
                    for (Class<? extends BuildHook> b : h.value()) {
                        if (BundleHook.class.isAssignableFrom(b)) {
                            Infuser.Builder builder = Infuser.builder(MethodHandles.lookup(), b, Class.class);
                            builder.provide(new Key<Class<? extends Assembly >>() {}).adaptArgument(0);
                            // If it is only ServiceExtension that ends up using it lets just dump it and have a single cast
                            // builder.provideHidden(ExtensionSetup.class).adaptArgument(0);
                            // Den den skal nok vaere lidt andet end hidden. Kunne kunne klare Optional osv
                            // MethodHandle mh =
                            // ExtensionSetup.MH_INJECT_PARENT.asType(ExtensionSetup.MH_INJECT_PARENT.type().changeReturnType(extensionClass));
                            // builder.provideHidden(extensionClass).invokeExact(mh, 0);

                            // Find a method handle for the extension's constructor

                            MethodHandle constructor = builder.findConstructor(BundleHook.class, m -> new InternalExtensionException(m));

                            BundleHook instance;
                            try {
                                instance = (BundleHook) constructor.invokeExact(type);
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            return new BundleModel(hooks.toArray(s -> new BundleHook[s]));
        }
    };

    public void preBuild(BundleConfiguration configuration) {
        for (BundleHook h : hooks) {
            h.beforeBuild(configuration);
        }
    }

    public void postBuild(BundleConfiguration configuration) {
        for (BundleHook h : hooks) {
            h.afterBuild(configuration);
            ;
        }
    }

    public static BundleModel of(Class<?> assemblyOrComposer) {
        return MODELS.get(assemblyOrComposer);
    }
}

class ContainerBuildHookModel {
    final BundleHook hook;

    ContainerBuildHookModel(Builder builder) {
        this.hook = null;
    }

    static class Builder {

    }
}
