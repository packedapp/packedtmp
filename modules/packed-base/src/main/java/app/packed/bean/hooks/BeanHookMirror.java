package app.packed.bean.hooks;

public interface BeanHookMirror {

    Class<?> activator();
    
    BeanHookKind kind();
}
