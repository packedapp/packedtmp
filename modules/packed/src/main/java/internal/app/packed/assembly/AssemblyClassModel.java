package internal.app.packed.assembly;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import app.packed.assembly.Assembly;
import app.packed.assembly.AssemblyBuildHook;
import app.packed.assembly.AssemblyConfiguration;
import app.packed.assembly.DelegatingAssembly;
import app.packed.build.BuildException;
import app.packed.build.hook.UseBuildHooks;
import app.packed.build.hook.BuildHook;
import internal.app.packed.bean.scanning.BeanTriggerModelCustom;
import internal.app.packed.build.hook.BuildHookMap;
import internal.app.packed.build.hook.StaticBuildHookMap;
import internal.app.packed.invoke.ConstructorSupport;
import internal.app.packed.invoke.ConstructorSupport.BuildHookFactory;

/** A model of an {@link Assembly} class. */
public final /* primitive */ class AssemblyClassModel {

    /** Cached models of assembly classes. */
    private final static ClassValue<AssemblyClassModel> MODELS = new ClassValue<>() {

        @Override
        protected AssemblyClassModel computeValue(Class<?> type) {
            HashMap<Class<? extends BuildHook>, List<BuildHook>> hookMap = new HashMap<>();
            for (Annotation a : type.getAnnotations()) {
                if (a instanceof UseBuildHooks h) {
                    for (Class<? extends BuildHook> b : h.hooks()) {
                        Class<? extends BuildHook> hookType = BuildHookMap.classOf(b);
                        BuildHookFactory factory = ConstructorSupport.newBuildHookFactory(type, h, b);
                        // Would be great with some caching. But it is tricky, we need to take into account
                        // if the assembly can both see the constructor of the buildhook
                        BuildHook instance = factory.create();
                        hookMap.computeIfAbsent(hookType, _ -> new ArrayList<>()).add(instance);
                    }
                }
            }
            if (!hookMap.isEmpty() && DelegatingAssembly.class.isAssignableFrom(type)) {
                throw new BuildException("Delegating assemblies cannot use @" + UseBuildHooks.class.getSimpleName() + " annotations, assembly type =" + type);
            }
            return new AssemblyClassModel(type, new StaticBuildHookMap(hookMap));
        }
    };

    public final BeanTriggerModelCustom hookModel;

    public final StaticBuildHookMap hooks;

    private AssemblyClassModel(Class<?> assemblyClass, StaticBuildHookMap hm) {
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
    public static AssemblyClassModel of(Class<? extends Assembly> assemblyClass) {
        return MODELS.get(assemblyClass);
    }
}
