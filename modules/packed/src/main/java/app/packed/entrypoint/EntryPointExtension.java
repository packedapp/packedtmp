package app.packed.entrypoint;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanIntrospector;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.Extension;
import app.packed.extension.ExtensionPoint;
import app.packed.extension.FrameworkExtension;
import app.packed.framework.Nullable;
import app.packed.operation.Op;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import internal.app.packed.application.ApplicationSetup;
import internal.app.packed.bean.BeanSetup;
import internal.app.packed.container.ContainerSetup;
import internal.app.packed.container.ExtensionSetup;
import internal.app.packed.entrypoint.EntryPointSetup;
import internal.app.packed.entrypoint.EntryPointSetup.MainThreadOfControl;

/**
 * An extension that controls entry points into an application.
 */
// Entrypoint er maaske daarligt. Giver ikke rigtig at sige en applikationer ikke
// har entry points

// ExecutionModel
public class EntryPointExtension extends FrameworkExtension<EntryPointExtension> {

    /** The configuration of the application. */
    final ApplicationSetup application;

    boolean hasMain;

    /** An object that is shared between all entry point extensions in the same application. */
    final ApplicationShare shared;

    /**
     * Create a new service extension.
     * 
     * @param configuration
     *            an extension configuration object.
     */
    /* package-private */ EntryPointExtension() {
        ExtensionSetup setup = ExtensionSetup.crack(this);
        this.application = setup.container.application;
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
            public void hookOnAnnotatedMethod(Set<Class<? extends Annotation>> hooks, OperationalMethod method) {
                int index = registerEntryPoint(null, true);
                
                ContainerSetup container = BeanSetup.crack(method).container;
                
                container.lifetime.entryPoint = new EntryPointSetup();

                MainThreadOfControl mc = container.lifetime.entryPoint.mainThread();

                OperationTemplate temp = OperationTemplate.defaults().withReturnType(method.operationType().returnType());
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
            shared.takeOver(EntryPointExtension.class);
        } else {
            shared.takeOver(extensionType);
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

        BeanConfiguration ebc;

        /** All entry points. */
        final List<EntryPointConf> entrypoints = new ArrayList<>();

        MethodHandle[] entryPoints;

        void takeOver(Class<? extends Extension<?>> takeOver) {
            if (this.dispatcher != null) {
                if (takeOver == this.dispatcher) {
                    return;
                }
                throw new IllegalStateException();
            }
            this.dispatcher = takeOver;
            ebc = base().install(EntryPointDispatcher.class);
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

    void main(Op<?> operation) {}
}
