package app.packed.application.host;

import java.util.Set;

import app.packed.application.ApplicationProducer;
import app.packed.job.Job;
import app.packed.job.JobAssembly;

public interface AH2<A> {

    Set<A> guests();

    void installGuest(ApplicationProducer<? extends A> assembly);
}

class Foo extends JobAssembly<String> implements ApplicationProducer<Job<?>> {

    @Override
    protected void build() {

        // TODO Auto-generated method stub

    }

    public void foo(JobHost h) {
        h.installGuest(new Foo());
    }
}

interface JobHost extends AH2<Job<?>> {

    // void installGuest2(Application2Assembly<? extends Job<?>, ?> assembly);
}