package app.packed.concurrent.scheduling;

import app.packed.context.Context;
import app.packed.extension.BeanHook.BindingTypeHook;

@BindingTypeHook(extension = SchedulingExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<SchedulingExtension> /* extends AttributedElement */ {

    long invocationCount();

    void pause();

    void resume();

    void cancel();
}
