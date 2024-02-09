package internal.app.packed.container;

import static java.util.Objects.requireNonNull;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyConfiguration;
import app.packed.assembly.TransformAssembly;
import app.packed.assembly.AssemblyTransformer;
import app.packed.assembly.DelegatingAssembly;
import app.packed.build.BuildException;
import internal.app.packed.bean.BeanHookModel;
import internal.app.packed.util.ThrowableUtil;

/** A model of an {@link Assembly} class. */
public final /* primitive */ class AssemblyModel {

    /** Cached models of assembly classes. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            ArrayList<AssemblyTransformer> hooks = new ArrayList<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof TransformAssembly h) {
                    for (Class<? extends AssemblyTransformer> b : h.value()) {
                        if (AssemblyTransformer.class.isAssignableFrom(b)) {
                            MethodHandle constructor;

                            if (!AssemblyModel.class.getModule().canRead(type.getModule())) {
                                AssemblyModel.class.getModule().addReads(type.getModule());
                            }

                            Lookup privateLookup;
                            try {
                                privateLookup = MethodHandles.privateLookupIn(type, MethodHandles.lookup() /* lookup */);
                            } catch (IllegalAccessException e1) {
                                throw new RuntimeException(e1);
                            }
                            // TODO fix visibility
                            // Maybe common findConstructorMethod
                            try {
                                constructor = privateLookup.findConstructor(b, MethodType.methodType(void.class));
                            } catch (NoSuchMethodException e) {
                                throw new BuildException("A container hook must provide an empty constructor, hook = " + h, e);
                            } catch (IllegalAccessException e) {
                                throw new BuildException("Can't see it sorry, hook = " + h, e);
                            }
                            constructor = constructor.asType(MethodType.methodType(AssemblyTransformer.class));

                            AssemblyTransformer instance;
                            try {
                                instance = (AssemblyTransformer) constructor.invokeExact();
                            } catch (Throwable t) {
                                throw ThrowableUtil.orUndeclared(t);
                            }
                            hooks.add(instance);
                        }
                    }
                }
            }
            if (!hooks.isEmpty() && DelegatingAssembly.class.isAssignableFrom(type)) {
                throw new BuildException("Delegating assemblies cannot use @" + TransformAssembly.class.getSimpleName() + " annotations, assembly type =" + type);
            }
            return new AssemblyModel(type, hooks.toArray(s -> new AssemblyTransformer[s]));
        }
    };

    /** Any hooks that have been specified on the assembly. */
    private final AssemblyTransformer[] assemblyTransformers;

    public final BeanHookModel hookModel;

    private AssemblyModel(Class<?> assemblyClass, AssemblyTransformer[] hooks) {
        this.assemblyTransformers = requireNonNull(hooks);
        this.hookModel = BeanHookModel.of(assemblyClass);
    }

    public void postBuild(AssemblyConfiguration configuration) {
        // TODO I think we should run these in reverse order
        for (AssemblyTransformer h : assemblyTransformers) {
            h.afterBuild(configuration);
        }
    }

    public void preBuild(AssemblyConfiguration configuration) {
        for (AssemblyTransformer h : assemblyTransformers) {
            h.beforeBuild(configuration);
        }
    }

    /**
     * Return an assembly model for the specified class.
     *
     * @param assemblyClass
     *            the type of assembly to return a model for
     * @return a model for the specified assembly
     */
    public static AssemblyModel of(Class<? extends Assembly> assemblyClass) {
        return MODELS.get(assemblyClass);
    }
}
