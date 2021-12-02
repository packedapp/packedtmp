package app.packed.bean.hooks.usage;

import app.packed.bean.hooks.BeanHook;
import app.packed.bean.hooks.BeanMethod;
import app.packed.bean.hooks.customization.BeanHookMapper;
import app.packed.container.BaseAssembly;
import app.packed.inject.service.Provide;


// Her har vi specifikt ingen extension...

// Altsaa mapper vi ogsaa sub klasses and super klasses??? Hvis man har adgang til super klasser vil jeg mene.
// For sub klasses probably always
@BeanHookMapper(from = Provide.class, to = @BeanHook(methodAnnotatedAccessible = MyHook.class))
public abstract class SomeAssembly extends BaseAssembly {

}

class MyHook extends BeanMethod {

    @Override
    protected void bootstrap() {
        System.out.println("Method : " + method());
        
    }
}
