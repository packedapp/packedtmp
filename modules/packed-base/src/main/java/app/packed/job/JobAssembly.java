package app.packed.job;

import java.util.concurrent.Callable;

import app.packed.application.ApplicationAssembly;
import app.packed.application.ApplicationMirror;
import app.packed.application.Launcher;
import app.packed.service.ServiceConfiguration;

public abstract class JobAssembly<T> extends ApplicationAssembly<Job<T>> {

    protected final void simpleComputable(Callable<T> calculation) {}

    protected final <X> ServiceConfiguration<Launcher<X>> provideJobLauncher(JobAssembly<X> assembly) {
        return null;
    }

    public static ApplicationMirror mirrorOf(JobAssembly<?> assembly) {
        throw new UnsupportedOperationException();
    }
    
    public static <T> Job<T> start(JobAssembly<T> job) {
        throw new UnsupportedOperationException();
    }
}

class MyJob extends JobAssembly<String> {

    @Override
    protected void build() {
        simpleComputable(() -> "Asd");
    }

    public static void main(String[] args) {
        start(new MyJob()).get();
        
        mirrorOf(new MyJob()).print();
    }
}
