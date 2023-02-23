package app.packed.entrypoint;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.ApplicationLocal;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.FrameworkExtension;
import app.packed.extension.operation.OperationHandle;
import app.packed.extension.operation.OperationTemplate;
import app.packed.operation.Op;
import app.packed.operation.OperationConfiguration;
import app.packed.util.Nullable;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.entrypoint.EntryPointSetup.MainThreadOfControl;

/**
 * An extension that controls entry points into an application.
 */
// Entrypoint er maaske daarligt. Giver ikke rigtig at sige en applikationer ikke
// har entry points
public class EntryPointExtension extends FrameworkExtension<EntryPointExtension> {

    /** The configuration of the application. */
    // final ApplicationSetup application;

    boolean hasMain;

    /** An object that is shared between all entry point extensions in the same application. */
    final ApplicationShare shared;

    static final ApplicationLocal<ApplicationShare> SHARED = ApplicationLocal.of(ApplicationShare::new);

    /**
     * Create a new service extension.
     *
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ EntryPointExtension() {
        // ExtensionSetup setup = ExtensionSetup.crack(this);
        // this.application = setup.container.application;
        this.shared = parent().map(e -> e.shared).orElseGet(ApplicationShare::new);
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
            public void hookOnAnnotatedMethod(Annotation hook, BeanMethod method) {
                int index = registerEntryPoint(null, true);

                ContainerSetup container = BeanSetup.crack(method).container;

                container.lifetime.entryPoint = new EntryPointSetup();

                MainThreadOfControl mc = container.lifetime.entryPoint.mainThread();

                OperationTemplate temp = OperationTemplate.defaults().returnType(method.operationType().returnRawType());
                OperationHandle os = method.newOperation(temp);
                os.specializeMirror(() -> new EntryPointMirror(index));

                runOnCodegen(() -> mc.generatedMethodHandle = os.generateMethodHandle());
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    protected EntryPointExtensionMirror newExtensionMirror() {
        return new EntryPointExtensionMirror();
    }

    @Override
    protected ExtensionPoint<EntryPointExtension> newExtensionPoint() {
        return new EntryPointExtensionPoint();
    }

    int registerEntryPoint(Class<? extends Extension<?>> extensionType, boolean isMain) {

        // Jeg gaar udfra metoden er blevet populeret med hvad der er behov for.
        // Saa det er kun selve invokationen der sker her

        // method.reserveMethodHandle(EC);
        // Grim kode pfa ExtensoinSupportContest i constructoren
        if (extensionType == null) {
            shared.takeOver(applicationRoot(), EntryPointExtension.class);
        } else {
            shared.takeOver(applicationRoot(), extensionType);
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
    static class ApplicationShare {

        @Nullable
        Class<? extends Extension<?>> dispatcher;

        BeanConfiguration ebc;

        /** All entry points. */
        final List<EntryPointConf> entrypoints = new ArrayList<>();

        MethodHandle[] entryPoints;

        void takeOver(EntryPointExtension epe, Class<? extends Extension<?>> takeOver) {
            if (this.dispatcher != null) {
                if (takeOver == this.dispatcher) {
                    return;
                }
                throw new IllegalStateException();
            }
            this.dispatcher = takeOver;
            ebc = epe.provide(EntryPointDispatcher.class);
        }
    }

    static class EntryPointConf {

    }

    static class EntryPointDispatcher {
        EntryPointDispatcher() {}
    }
}

class Zandbox {

    // installMain
    // lazyMain()?
    <T extends Runnable> InstanceBeanConfiguration<?> installMain(Class<T> beanClass) {
        // IDK, skal vi vente med at tilfoeje dem
        throw new UnsupportedOperationException();
    }

    <T extends Runnable> InstanceBeanConfiguration<?> installMainInstance(T beanInstance) {
        throw new UnsupportedOperationException();
    }

    public OperationConfiguration main(Op<?> operation) {
        throw new UnsupportedOperationException();
    }
}
