package app.packed.entrypoint;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.base.Nullable;
import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanExtension;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.container.Extension;
import app.packed.container.Extension.DependsOn;
import app.packed.container.ExtensionPoint;
import app.packed.operation.InvocationType;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.application.EntryPointSetup;
import internal.app.packed.application.EntryPointSetup.MainThreadOfControl;
import internal.app.packed.bean.MethodIntrospector;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.operation.OperationSetup;

/**
 * An extension that controls entry points into an application.
 */
// Entrypoint er maaske daarligt. Giver ikke rigtig at sige en applikationer ikke
// har entry points

// ExecutionModel

@DependsOn(extensions = BeanExtension.class)
public class EntryPointExtension extends Extension<EntryPointExtension> {

    boolean hasMain;

    /** An object that is shared between all entry point extensions in the same application. */
    final ApplicationShare share;

    final ApplicationSetup application;

    private final ExtensionSetup setup = ExtensionSetup.crack(this);

    /**
     * Create a new service extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ EntryPointExtension() {
        ExtensionSetup setup = ExtensionSetup.crack(this);
        this.application = setup.container.application;
        this.share = parent().map(e -> e.share).orElseGet(ApplicationShare::new);
    }

    @Override
    protected ExtensionPoint<EntryPointExtension> newExtensionPoint() {
        return new EntryPointExtensionPoint();
    }

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            /**
             * Captures methods annotated with {@link Main}.
             * <p>
             * {@inheritDoc}
             */
            @Override
            public void onMethod(OnMethod method) {
                registerEntryPoint(null, true);

                application.entryPoints = new EntryPointSetup();

                MainThreadOfControl mc = application.entryPoints.mainThread();

                mc.isStatic = Modifier.isStatic(method.getModifiers());
                mc.cs = ((MethodIntrospector) method).introspector.bean;

                OperationSetup os = ((MethodIntrospector) method).newOperation(setup, InvocationType.defaults());
                mc.methodHandle = os.target.methodHandle;
            }

        };
    }

    // installMain
    public <T extends Runnable> InstanceBeanConfiguration<?> installMain(Class<T> beanClass) {
        // IDK, skal vi vente med at tilfoeje dem
        throw new UnsupportedOperationException();
    }

    public <T extends Runnable> InstanceBeanConfiguration<?> installMainInstance(T beanInstance) {
        throw new UnsupportedOperationException();
    }

    /** {@inheritDoc} */
    @Override
    protected EntryPointExtensionMirror newExtensionMirror() {
        return new EntryPointExtensionMirror();
    }

    int registerEntryPoint(Class<? extends Extension<?>> extensionType, boolean isMain) {

        // Jeg gaar udfra metoden er blevet populeret med hvad der er behov for.
        // Saa det er kun selve invokationen der sker her

        // method.reserveMethodHandle(EC);
        // Grim kode pfa ExtensoinSupportContest i constructoren
        if (extensionType == null) {
            share.takeOver(EntryPointExtension.class);
        } else {
            share.takeOver(extensionType);
        }
        if (isMain) {
            hasMain = true;
        }
        return 0;
    }

    public void setShutdownStrategy(Supplier<Throwable> maker) {
        // Ideen er lidt at sige hvad sker der paa runtime.
        // Hvis vi shutter down... Default er at der bliver
        // Sat en cancellation exception som reaason
        // CancellationException
        // Men ved ikke om vi skal have en special exception istedet for???
        // En checked istedet ligesom TimeoutException
    }

    /** An instance of this class is shared between all entry point extensions for a single application. */
    class ApplicationShare {

        @Nullable
        Class<? extends Extension<?>> dispatcher;

        /** All entry points. */
        final List<EntryPointConf> entrypoints = new ArrayList<>();

        MethodHandle[] entryPoints;

        BeanConfiguration ebc;

        void takeOver(Class<? extends Extension<?>> takeOver) {
            if (this.dispatcher != null) {
                if (takeOver == this.dispatcher) {
                    return;
                }
                throw new IllegalStateException();
            }
            this.dispatcher = takeOver;
            ebc = bean().install(EntryPointDispatcher.class);
        }
    }

    static class EntryPointDispatcher {
        EntryPointDispatcher() {}
    }

    static class EntryPointConf {

    }
}
//@Override
//protected void onApplicationClose() {
//  if (isRootOfApplication()) {
//      // Her installere vi MethodHandles der bliver shared, taenker det er bedre end at faa injected
//      // extensions'ene
//
//      // install
//      // provide (visible i mxxz;ias:"?aZ.a:n aZz¸ m nl jj m ,m ;n .n ≥ en container)
//      // provideShared (container + subcontainers)
//      // shareInstance(new MethodHandle[0]);
//  }
//  super.onApplicationClose();
//}