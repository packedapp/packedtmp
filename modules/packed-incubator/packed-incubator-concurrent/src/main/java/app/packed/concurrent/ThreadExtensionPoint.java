package app.packed.concurrent;

import app.packed.extension.ExtensionPoint;

public class ThreadExtensionPoint extends ExtensionPoint<ThreadExtension> {

    ThreadExtensionPoint() {}

    /**
     * @param object
     * @return
     */
    public ScheduledOperationConfiguration schedule(Object object) {
        throw new UnsupportedOperationException();
    }


}
