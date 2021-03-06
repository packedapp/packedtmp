package app.packed.application;

import app.packed.component.BeanMirror;
import app.packed.component.Wirelet;

public abstract class DaemonAssembly extends ApplicationAssembly<Daemon> {

    protected final void restartOnAnyException() {
        //Must be a fcking extension...
    }

    // public static void start(DeamonAssembly assembly) {}

    public static void start(DaemonAssembly assembly, Wirelet... wirelets) {}

//    public static void start(DeamonAssembly assembly, String[] args) {}

    public static void start(DaemonAssembly assembly, String[] args, Wirelet... wirelets) {}

//    public static ApplicationMirror mirror(DeamonAssembly assembly) {
//        throw new UnsupportedOperationException();
//    }

    public static ApplicationMirror mirror(DaemonAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }

    public static ApplicationImage<Daemon> imageOf(DaemonAssembly assembly, Wirelet... wirelets) {
        throw new UnsupportedOperationException();
    }
}

class MyDaemonAssembly extends DaemonAssembly {

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

class XDaemonAssembly extends DaemonAssembly {

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