package app.packed.bean.archive;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Map;

import app.packed.bean.BeanCustomizer;
import app.packed.component.UserOrExtension;
import app.packed.extension.Extension;

public class ZestExtension extends Extension<ZestExtension> {

    private ArrayList<MethodHandle> handles = new ArrayList<>();

//    public <T> ManagedBeanConfiguration<T> install(Class<T> stuff) {
//        BeanSupport2 b = use(BeanSupport2.class);
//        // TROR ikke man kan lave MH før i onClose..
//        // Vi kender jo ikke noedvendig id'et foer senere
//        ManagedBeanConfiguration<T> conf = b.register(null, BeanDriver.builder().build(), new ManagedBeanConfiguration<>(), stuff);
//        handles.add(b.processor(conf));
//        return conf;
//    }

    public <T> ManagedBeanConfiguration<T> install2(Class<T> stuff) {
        BeanCustomizer<T> maker = bean().newCustomizer(UserOrExtension.user(), stuff);

        //// Det er vel bestemt udfra typen
        // access singlton / makeNew
        
        // void maker.addInvocationArgument(Class<?> ) extra param to mh
        // MethodType invocationType();
        // Som er void runtimeExtensionContext som default
        // Hmm depends on the bean stereotype...
        // fx RequestBeans er altid void.
        
        // Kan vi have multiple invocation types??
        // fx create + shutdown each taking different parameters.
        // fx shutdown Entity may take the entity
        
        
        // maker.runOnClose(handles.add...)
        // handle.lifetime.

        return new ManagedBeanConfiguration<>(maker);
    }

    @Override
    protected void onClose() {
        // for each handle-> resolve to MethodHandle

        bean().install(RuntimeBean.class).inject(handles.toArray(i -> new MethodHandle[i]));
        handles = null;
    }

    // primitive record
    static class RuntimeBean {
        final MethodHandle[] mh;
        final RuntimeExtensionContext ec;
        final Map<Class<?>, Integer> mo;

        RuntimeBean(RuntimeExtensionContext ec, Map<Class<?>, Integer> mo, MethodHandle[] mh) {
            this.ec = ec;
            this.mh = mh;
            this.mo = mo;
        }

        public Object getIt(Class<?> cl) {
            Integer i = mo.get(cl);
            if (i == null) {
                throw new UnsupportedOperationException("Unknown service");
            }
            try {
                return mh[i].invokeExact(ec);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    // BeanContext???? IDK alle beans fra samme extension kan vel bruge dem?
    
    interface RuntimeExtensionContext {}
}
