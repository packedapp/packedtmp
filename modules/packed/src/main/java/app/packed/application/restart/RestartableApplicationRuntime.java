package app.packed.application.restart;

import app.packed.lifecycle.LifecycleApplicationController;

public interface RestartableApplicationRuntime {

    LifecycleApplicationController current();
}
