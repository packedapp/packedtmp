package app.packed.application.programs;

import app.packed.lifecycle.LifecycleApplicationController;

public interface RestartableApplicationRuntime {

    LifecycleApplicationController current();
}
