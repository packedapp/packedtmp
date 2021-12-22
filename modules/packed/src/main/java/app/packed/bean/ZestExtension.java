package app.packed.bean;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.Map;

import app.packed.extension.Extension;

public class ZestExtension extends Extension {

    private ArrayList<MethodHandle> handles = new ArrayList<>();

    public <T> ManagedBeanConfiguration<T> install(Class<T> stuff) {
        BeanSupport2 b = use(BeanSupport2.class);
        ManagedBeanConfiguration<T> conf = b.register(null, BeanDriver.builder().build(), new ManagedBeanConfiguration<>(), stuff);
        
        handles.add(b.processor(conf)); 
        
        return conf;
    }

    @Override
    protected void onComplete() {
        use(BeanSupport2.class).install(RuntimeBean.class).inject(handles.toArray(i -> new MethodHandle[i]));
        handles = null;
    }

    static class RuntimeBean {
        final MethodHandle[] mh;
        final ExtensionContext ec;
        final Map<Class<?>, Integer> mo;

        RuntimeBean(ExtensionContext ec, Map<Class<?>, Integer> mo, MethodHandle[] mh) {
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

    interface ExtensionContext {}
}
