package app.packed.concurrent.other;

import app.packed.context.Context;
import app.packed.context.ContextualServiceProvider;

@ContextualServiceProvider(extension = ScheduledJobExtension.class, requiresContext = SchedulingContext.class)
public interface SchedulingContext extends Context<ScheduledJobExtension> /* extends AttributedElement */ {

    void pause();

    void resume();

    void cancel();
}
