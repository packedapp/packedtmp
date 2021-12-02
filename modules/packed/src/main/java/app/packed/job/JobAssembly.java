package app.packed.job;

import java.util.concurrent.Callable;

import app.packed.application.ApplicationImage;
import app.packed.application.ApplicationMirror;
import app.packed.container.BaseAssembly;
import app.packed.inject.service.ServiceConfiguration;

public abstract class JobAssembly<T> extends BaseAssembly {

    protected JobAssembly() {
        super(null);
    }

    protected final void simpleComputable(Callable<T> calculation) {}

    protected final <X> ServiceConfiguration<ApplicationImage<X>> provideJobLauncher(JobAssembly<X> assembly) {
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
