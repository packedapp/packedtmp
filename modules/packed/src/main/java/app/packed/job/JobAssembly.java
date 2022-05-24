package app.packed.job;

import java.util.concurrent.Callable;

import app.packed.application.ApplicationLauncher;
import app.packed.application.ApplicationMirror;
import app.packed.container.BaseAssembly;
import packed.internal.inject.service.ServiceConfiguration;

public abstract class JobAssembly<T> extends BaseAssembly {


    protected final void simpleComputable(Callable<T> calculation) {}

    protected final <X> ServiceConfiguration<ApplicationLauncher<X>> provideJobLauncher(JobAssembly<X> assembly) {
        return null;
    }

    public static ApplicationMirror mirrorOf(JobAssembly<?> assembly) {
        throw new UnsupportedOperationException();
    }
    
    public static <T> Job<T> start(JobAssembly<T> job) {
        throw new UnsupportedOperationException();
    }
}

class MyJobAssembly extends JobAssembly<String> {

    @Override
    protected void build() {
        simpleComputable(() -> "Asd");
    }

    public static void main(String[] args) {
        start(new MyJobAssembly()).get();
        
        mirrorOf(new MyJobAssembly()).print();
    }
}
