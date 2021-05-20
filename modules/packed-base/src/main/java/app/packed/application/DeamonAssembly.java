package app.packed.application;

import app.packed.component.BeanMirror;
import app.packed.component.Wirelet;
import app.packed.container.BaseAssembly;

public abstract class DeamonAssembly extends BaseAssembly {

    protected final void restartOnAnyException() {

    }

    public static void start(DeamonAssembly assembly) {}

    public static void start(DeamonAssembly assembly, Wirelet... wirelets) {}

    public static void start(DeamonAssembly assembly, String[] args) {}

    public static void start(DeamonAssembly assembly, String[] args, Wirelet... wirelets) {}

    
    public static ApplicationMirror mirror(DeamonAssembly assembly) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationMirror mirror(DeamonAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationImage<Daemon> imageOf(DeamonAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

class MyDaemonAssembly extends DeamonAssembly {

    @Override
    protected void build() {
        restartOnAnyException();
        provideInstance("ASDASD");
        link(new XDaemonAssembly());
    }

    public static void main(String[] args) {
        start(new MyDaemonAssembly(), args);
        mirror(new MyDaemonAssembly()).select(BeanMirror.class).allMatch(m -> m.beanType() == String.class);

        System.out.println("Any string beans " + mirror(new MyDaemonAssembly()).selectBeans().anyMatch(m -> m.beanType() == String.class));
    }
}

class XDaemonAssembly extends DeamonAssembly {

    static final ApplicationImage<Daemon> IMG = imageOf(new XDaemonAssembly());

    @Override
    protected void build() {
        restartOnAnyException();
        provideInstance("ASDASD");
    }

    public static void main(String[] args) {
        IMG.launch();
    }
}