package app.packed.concurrent.job;

import app.packed.concurrent.ScheduledOperationConfiguration;
import app.packed.extension.ExtensionPoint;

public class JobExtensionPoint extends ExtensionPoint<JobExtension> {

    JobExtensionPoint(ExtensionUseSite usesite) {
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
