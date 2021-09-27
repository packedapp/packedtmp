package app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Set;

import app.packed.component.ComponentNewMirror;
import packed.internal.component.ComponentSetup;
import packed.internal.component.bean.BeanSetup;
import packed.internal.util.LookupUtil;
import packed.internal.util.ThrowableUtil;

public non-sealed class BeanNewMirror extends ComponentNewMirror {

    /** A handle that can access superclass private ComponentConfiguration#component(). */
    private static final MethodHandle MH_COMPONENT_CONFIGURATION_COMPONENT = MethodHandles.explicitCastArguments(
            LookupUtil.lookupVirtualPrivate(MethodHandles.lookup(), ComponentNewMirror.class, "component", ComponentSetup.class),
            MethodType.methodType(BeanSetup.class, BeanNewMirror.class));

    
    BeanSetup bean;
    
    protected BeanNewMirror() {}

    private BeanSetup bean() {
        try {
            return (BeanSetup) MH_COMPONENT_CONFIGURATION_COMPONENT.invokeExact(this);
        } catch (Throwable e) {
            throw ThrowableUtil.orUndeclared(e);
        }
    }
    
    public static void main(String[] args) {
        new BeanNewMirror().beanType();
    }

    /** {@return the type (class) of the bean.} */
    public final Class<?> beanType() {
        return bean().hookModel.clazz;
    }

    /** {@return all hooks that have been applied on the bean.} */
    public final Set<?> hooks() {
        throw new UnsupportedOperationException();
    }

    public final <T /* extends HookMirror */> Set<?> hooks(Class<T> hookType) {
        throw new UnsupportedOperationException();
    }

    /** {@return the kind of the bean.} */
    public BeanKind kind() {
        throw new UnsupportedOperationException();
    }
}
