package app.packed.bean.hooks.usage;

import app.packed.bean.hooks.ApplyBeanHook;
import app.packed.bean.hooks.BeanAnnotationMapning;
import app.packed.bean.hooks.BeanMethodHook;
import app.packed.container.BaseAssembly;
import app.packed.service.Provide;


// Her har vi specifikt ingen extension...

// Altsaa mapper vi ogsaa sub klasses and super klasses??? Hvis man har adgang til super klasser vil jeg mene.
// For sub klasses probably always
@BeanAnnotationMapning(annotation = Provide.class, to = @ApplyBeanHook(methodAccessible = MyHook.class))
public abstract class SomeAssembly extends BaseAssembly {

}

class MyHook extends BeanMethodHook {

    @Override
    protected void bootstrap() {
        System.out.println("Method : " + method());
    }
}
