package packed.internal.hooks.bootstrap;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import app.packed.hooks.FieldHook;
import packed.internal.hooks.ClassHookModel;
import packed.internal.hooks.usesite.UseSiteFieldHookModel;
import packed.internal.util.LookupUtil;

public class FieldHookModel {

    // Skal ikke vaere her... Men paa builderen...
    public static void onMethod(ClassHookModel.Builder classBuilder, Method method) {

        // Vi bliver noedt til at scanne alle annoteringer samlet..
        // Syntes ikke vi skal paa begynde at lave hooks...
        // Hvis vi f.eks. har 2 inject hook
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a = annotations[i];
            AbstractFieldOrInjectableVariableHook h = classBuilder.forField(a.annotationType());
            if (h != null) {
                for (int j = i; j < annotations.length; j++) {
                    Annotation b = annotations[j];
                    AbstractFieldOrInjectableVariableHook g = classBuilder.forField(b.annotationType());
                    if (g != null) {
                                                
                        // Multiple hooked annotations
                        // Might need to copy Field...
                        // Might fail if the hooks are not compatible.. after some rules
                    }
                }
                // Found a single hooked annotated...
                
                
                return;
            }
        }
    }

    
    // Skal ikke vaere her... Men paa builderen...
    public static void onField(ClassHookModel.Builder classBuilder, Field field) {

        // Vi bliver noedt til at scanne alle annoteringer samlet..
        // Syntes ikke vi skal paa begynde at lave hooks...
        // Hvis vi f.eks. har 2 inject hook
        Annotation[] annotations = field.getAnnotations();
        for (int i = 0; i < annotations.length; i++) {
            Annotation a = annotations[i];
            AbstractFieldOrInjectableVariableHook h = classBuilder.forField(a.annotationType());
            if (h != null) {
                for (int j = i; j < annotations.length; j++) {
                    Annotation b = annotations[j];
                    AbstractFieldOrInjectableVariableHook g = classBuilder.forField(b.annotationType());
                    if (g != null) {
                                                
                        // Multiple hooked annotations
                        // Might need to copy Field...
                        // Might fail if the hooks are not compatible.. after some rules
                    }
                }
                // Found a single hooked annotated...
                
                
                return;
            }
        }
    }

    static class BootstrapModel {

        /** A MethodHandle that can invoke {@link FieldHook.Bootstrap#bootstrap}. */
        static final MethodHandle MH_FIELD_HOOK_BOOTSTRAP = LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class, "bootstrap",
                void.class);

        /** A VarHandle that can access {@link FieldHook.Bootstrap#builder}. */
        static final VarHandle VH_FIELD_HOOK_BUILDER = LookupUtil.lookupVarHandlePrivate(MethodHandles.lookup(), FieldHook.Bootstrap.class, "builder",
                UseSiteFieldHookModel.Builder.class);
    }

    static class BootstrapModelLoader {}

}
