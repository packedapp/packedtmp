package app.packed.concurrent.scheduling;

import app.packed.bean.BeanHook.BindingTypeHook;
import app.packed.context.Context;

@BindingTypeHook(extension = SchedulingExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<SchedulingExtension> /* extends AttributedElement */ {

    long invocationCount();
    
    void pause();

    void resume();

    void cancel();
}
