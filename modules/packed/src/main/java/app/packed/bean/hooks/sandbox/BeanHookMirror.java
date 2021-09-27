package app.packed.bean.hooks.sandbox;

public interface BeanHookMirror {

    Class<?> activator();
    
    BeanHookKind kind();
}
