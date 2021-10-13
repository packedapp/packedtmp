package app.packed.schedule;

import app.packed.bean.BeanConfiguration;
import app.packed.bundle.Assembly;
import app.packed.bundle.BundleMirror;
import app.packed.bundle.Wirelet;
import app.packed.component.FunctionConfiguration;
import app.packed.extension.Extension;

// Igen ligesom WebRequest.
// Har ApplicationBean.schedule
// ScheduleBean.schedule
// Application->

// Syntes vi smider den ind .time???
public class ScheduleExtension extends Extension {

    public <T extends Runnable> FunctionConfiguration fSchedule(Runnable runnable) {
        throw new UnsupportedOperationException();
    }

//    public <T extends Runnable> BeanConfiguration<T> scheduleRunnable(Class<T> cl) {
//        throw new UnsupportedOperationException();
//    }

    // Enten skal man have en Runnable
    public <B> BeanConfiguration<B> schedule(Class<B> cl) {
        throw new UnsupportedOperationException();
    }

    // Skal returne noget LinkedContainer
    // Maaske kan man specificere en Wirelet...
    // Spawn as Application...
    public BundleMirror schedule(Assembly  bundle, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}
