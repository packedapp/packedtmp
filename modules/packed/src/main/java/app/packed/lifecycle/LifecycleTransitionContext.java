package app.packed.lifecycle;

public interface LifecycleTransitionContext {

    void awaitIfPresent(WaitToken token);

    static interface WaitToken {}

    // Gemmer den i en statisk felt...
    //
    static interface TokenHolder {
        WaitToken wt();

        void release();
    }

    // Altsaa vi kunne godt have noget deling her...

    // Vi har noget Scope (andet navn) Application Instance, Component Instance

    // Hvis man er pool kan man kun faa Component Instance Scope
    // Hvis man er singleton faar man Application Instance Scope

    // Applet... -> Bound to the lifecycle of the application
    // There is a single
    public interface InitializationContext {

    }
}
