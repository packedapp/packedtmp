package app.packed.concurrent.other;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import app.packed.assembly.Assembly;
import app.packed.bean.InstanceBeanConfiguration;
import app.packed.extension.BeanElement.BeanMethod;
import app.packed.extension.BeanIntrospector;
import app.packed.extension.Extension.DependsOn;
import app.packed.extension.UnwrappedBindableVariable;
import app.packed.operation.Op;
import app.packed.operation.Op1;
import app.packed.operation.OperationHandle;
import app.packed.operation.OperationTemplate;
import extensions.IncubatorExtension;
import extensions.time.TimeExtension;
import internal.app.packed.context.publish.ContextTemplate;

// Tror den er separat fra Time extension

// Schedule of class/factory/runnable

// schedule(Runnable).atFixedRate();

// Det der ikke er muligt er.
// Single instance, programmatically set timer...
// Schedule prototype();

// Control af existerende tasks...

// Altsaa maaske skal man bruge

// Maybe just a JobExtension...

/**
 * An extension that deals with schedulation of tasks.
 */
// Or do we schedule jobs...
// I think we maybe schedule jobs
// Duration ->

// schedule(Class), atFixedRate());

// start(Class), onTrigger());
// start(Class), onContainerStartup());
// start(Class), onContainerShutdown());

// Was ScheduledExtension, maybe it ends up just being JobExtension...
@DependsOn(extensions = TimeExtension.class)
public class ScheduledJobExtension extends IncubatorExtension<ScheduledJobExtension> {

    // Must have either an runnable or a single @Schedule method

    private static final ContextTemplate CT = ContextTemplate.of(MethodHandles.lookup(), SchedulingContext.class, PackedSchedulingContext.class);

    private static final OperationTemplate OT = OperationTemplate.defaults().reconfigure(c -> c.inContext(CT));

    ScheduledJobExtension() {}
    // Creates a new instance on every invocation

    @Override
    protected BeanIntrospector newBeanIntrospector() {
        return new BeanIntrospector() {

            @SuppressWarnings("unused")
            @Override
            public void triggeredByAnnotatedMethod(Annotation hook, BeanMethod method) {
                Cron c = method.annotations().readRequired(Cron.class);

                OperationHandle<?> operation = method.newOperation(OT).install(OperationHandle::new);

                InstanceBeanConfiguration<SchedulingBean> bean = lifetimeRoot().base().installIfAbsent(SchedulingBean.class, handle -> {
                    handle.bindCodeGenerator(MethodHandle.class, () -> operation.generateMethodHandle());
                });
                // bean, add scheduling +
                // Manytons dur ikke direkte

                //
                // bean.addSchedule
                // parse expresion

            }

            @Override
            public void activatedByVariableType(Class<?> hook, Class<?> actualHook, UnwrappedBindableVariable binding) {
                if (hook == SchedulingContext.class) {
                    binding.bindContext(SchedulingContext.class);
                } else if (hook == SchedulingHistory.class) {
                    binding.bindOp(new Op1<PackedSchedulingContext, SchedulingHistory>(c -> c.history) {});

                    // binding.bindOp(new Op1<PackedSchedulingContext, SchedulingHistory>(c -> c.history) {});
                } else {
                    super.activatedByVariableType(hook, actualHook, binding);
                }
            }
        };
    }

    //
    public ScheduledOperationConfiguration schedule(Assembly assembly) {
        // Skal vi have link med i navnet
        throw new UnsupportedOperationException();
    }

    // If the class implements Runnable -> run with be invoked
    // Otherwise will look for exactly 1 @Schedule annotation
//    /**
//     * T
//     *
//     * @param clazz
//     *            the clazz
//     * @return
//     * @throws IllegalArgumentException
//     *             if the specified class is not assignable to {@link Runnable}. Or if the class does not have exactly one
//     *             method annotated with {@link Schedule}
//     */
//    public ScheduledOperationConfiguration schedule(Class<?> clazz) {
//        throw new UnsupportedOperationException();
//    }

    // Nej det er sgu Op1... taenker jeg...
//    public ScheduledOperationConfiguration scheduleOperation(Consumer<SchedulingContext> action) {
//        throw new UnsupportedOperationException();
//    }

    // Unlike install this will create a new instance every time
    // OHH wow, we can ikke bruge Factory her. Vi skal jo ikke registrere noget
    // Vi har brug for Op... Som kan tage nogle parametere..

    /**
     * Schedules an operation.
     *
     * @param op
     *            the operation that will be invoked
     * @return a configuration object representing the scheduled operation
     */
    public ScheduledOperationConfiguration schedule(Op<?> op) {
        throw new UnsupportedOperationException();
    }

//    // Den her er jo functionel... Ohh wow
//    // Tror bare vi dropper den her
//    public ScheduledOperationConfiguration scheduleOperation(Runnable runnable) {
//        return scheduleOperation(Op.ofRunnable(runnable));
//    }

//    // Will spawn a new fcking component on every xxx
//    public ScheduledOperationConfiguration scheduleOperationPrototype(Op<?> factory) {
//        throw new UnsupportedOperationException();
//    }

    private static class SchedulingBean {}

    // How do we control
    /// Threads
    /// Retry policies
    /// Error handling

    // We most likely want to have a
    // standard container (namespace inherited way).
    // And then an explicit way...

    // Altsaa hvad hvis vi reler paa defaulten...
    // Og ikke vil have den overskrives af foraeldren...
}
// ScheduledComponentConfiguration
// Was (below). Men giver god mening at splitte den op i 2...

//
//public ComponentConfiguration scheduleAtFixedRate(Class<?> clazz, long initialDelay, long period, TimeUnit unit) {
//
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleAtFixedRate(Class<?> clazz, Duration duration) {
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleWithFixedDelay(Class<?> clazz, long duration, TimeUnit unit) {
//  throw new UnsupportedOperationException();
//}
//
//public ComponentConfiguration scheduleWithFixedDelay(Class<?> clazz, Duration duration) {
//  throw new UnsupportedOperationException();
//}