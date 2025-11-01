package app.packed.concurrent.annotations;

import app.packed.bean.BeanIntrospector;
import app.packed.bean.BeanTrigger.AutoInject;
import app.packed.binding.Key;
import app.packed.concurrent.other.ScheduledJobExtension;
import app.packed.context.Context;

@AutoInject(introspector = ScheduledJobContextBeanIntrospector.class, requiresContext = ScheduledJobContext.class)
public interface ScheduledJobContext extends Context<ScheduledJobExtension> {

    void pause();

    void resume();

    void cancel();
}

final class ScheduledJobContextBeanIntrospector extends BeanIntrospector<ScheduledJobExtension> {

    @Override
    public void onExtensionService(Key<?> key, OnContextService service) {
        service.binder().bindContext(ScheduledJobContext.class);
    }
}
