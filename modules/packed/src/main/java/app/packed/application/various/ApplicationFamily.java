package app.packed.application.various;

//Or stereotype
// Or ApplicationKind
public enum ApplicationFamily {

    Doo,

    //
    // Bootstrap a single other Application after initialization (via an entry point)
    BOOTSTRAPPER,

    // Has Runtime
    // Has result, possible Void
    // Control: Process - Start - Cancel -
    JOB,

    // Has Runtime
    // May be Restartable, Suspendable
    // No result
    // No entry
    // Control: Process - start/stop
    DAEMON;
}
enum ExecutionMode {
    NONE, DAEMON, JOB, CLI;
  }
  // Entrypoints...

  // CLI og start er lidt sjov... 
  // Job er som en cli, med kun et entrypoint og en retur vaerdi...

  // CLI -> Help (no start) Foo-> New App