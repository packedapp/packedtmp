package app.packed.time;

import java.util.concurrent.TimeUnit;

import app.packed.component.BaseComponentConfiguration;
import app.packed.component.ComponentConfigurationContext;

public class ScheduledComponentConfiguration extends BaseComponentConfiguration {

    public ScheduledComponentConfiguration(ComponentConfigurationContext context) {
        super(context);
    }

    public ScheduledComponentConfiguration atFixedRate(long initialDelay, long period, TimeUnit unit) {
        return this;
    }
    
    // timezone??
    // override time extension
    
    // from starting
    // from start
    // start paused.. must use schedulingContext.resume;
    // 
}
