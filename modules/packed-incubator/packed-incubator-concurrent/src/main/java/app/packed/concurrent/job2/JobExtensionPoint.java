package app.packed.concurrent.job2;

import app.packed.concurrent.ScheduledOperationConfiguration;
import app.packed.extension.ExtensionPoint;

public class JobExtensionPoint extends ExtensionPoint<JobExtension> {

    JobExtensionPoint(ExtensionPointHandle usesite) {
        super(usesite);
    }

    /**
     * @param object
     * @return
     */
    public ScheduledOperationConfiguration schedule(Object object) {
        throw new UnsupportedOperationException();
    }


}
