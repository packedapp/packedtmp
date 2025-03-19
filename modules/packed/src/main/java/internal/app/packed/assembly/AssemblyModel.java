package internal.app.packed.assembly;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyBuildHook;
import app.packed.assembly.AssemblyConfiguration;
import app.packed.assembly.DelegatingAssembly;
import app.packed.build.BuildException;
import app.packed.build.hook.ApplyBuildHook;
import app.packed.build.hook.BuildHook;
import internal.app.packed.bean.scanning.BeanTriggerModelCustom;
import internal.app.packed.build.hook.BuildHookMap;
import internal.app.packed.build.hook.StaticBuildHookMap;
import internal.app.packed.util.ThrowableUtil;

/** A model of an {@link Assembly} class. */
public final /* primitive */ class AssemblyModel {

    /** Cached models of assembly classes. */
    private final static ClassValue<AssemblyModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyModel computeValue(Class<?> type) {
            HashMap<Class<? extends BuildHook>, List<BuildHook>> hookMap = new HashMap<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof ApplyBuildHook h) {
                    for (Class<? extends BuildHook> b : h.hooks()) {
                        Class<? extends BuildHook> hookType = BuildHookMap.classOf(b);
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

                        // For consistency reasons we always tries to use invokeExact() even if not strictly needed
                        constructor = constructor.asType(MethodType.methodType(BuildHook.class));

                        BuildHook instance;
                        try {
                            instance = (BuildHook) constructor.invokeExact();
                        } catch (Throwable t) {
                            throw ThrowableUtil.orUndeclared(t);
                        }

                        hookMap.computeIfAbsent(hookType, _ -> new ArrayList<>()).add(instance);
                    }
                }
            }
            if (!hookMap.isEmpty() && DelegatingAssembly.class.isAssignableFrom(type)) {
                throw new BuildException("Delegating assemblies cannot use @" + ApplyBuildHook.class.getSimpleName() + " annotations, assembly type =" + type);
            }
            return new AssemblyModel(type,  new StaticBuildHookMap(hookMap));
        }
    };

    public final BeanTriggerModelCustom hookModel;

    public final StaticBuildHookMap hooks;

    private AssemblyModel(Class<?> assemblyClass, StaticBuildHookMap hm) {
        this.hookModel = BeanTriggerModelCustom.of(assemblyClass);
        this.hooks = hm;
    }

    public void postBuild(AssemblyConfiguration configuration) {
        hooks.forEach(AssemblyBuildHook.class, h -> h.afterBuild(configuration));
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
