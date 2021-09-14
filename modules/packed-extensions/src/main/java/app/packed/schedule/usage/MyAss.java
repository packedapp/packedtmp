package app.packed.schedule.usage;

import app.packed.container.BaseBundle;
import app.packed.schedule.ScheduleExtension;

public class MyAss extends BaseBundle {

    @Override
    protected void build() {

        // Must have at least one scheduling annotation
        use(ScheduleExtension.class).schedule(String.class);
    }

}
