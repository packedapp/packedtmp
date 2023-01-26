package app.packed.concurrent.scheduling;

import app.packed.bean.BeanHook.TypedProvisionHook;
import app.packed.context.Context;

@TypedProvisionHook(extension = SchedulingExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<SchedulingExtension> /* extends AttributedElement */ {

    long invocationCount();
    
    void pause();

    void resume();

    void cancel();
}
