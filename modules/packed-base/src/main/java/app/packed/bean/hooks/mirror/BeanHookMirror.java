package app.packed.bean.hooks.mirror;

public interface BeanHookMirror {

    Class<?> activator();
    
    BeanHookKind kind();
}
