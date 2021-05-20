package app.packed.job;

import java.util.concurrent.Callable;

import app.packed.application.Launcher;
import app.packed.container.BaseAssembly;
import app.packed.inject.ServiceConfiguration;

public abstract class JobAssembly<T> extends BaseAssembly {

    protected final void simpleComputable(Callable<T> calculation) {}

    
    protected final <X> ServiceConfiguration<Launcher<X>> provideJobLauncher(JobAssembly<X> assembly) {
        return null;
    }
    

}
