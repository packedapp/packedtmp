package app.packed.application.various;

enum ExecutionMode {
  NONE, DAEMON, JOB, CLI;
}
// Entrypoints...

// CLI og start er lidt sjov... 
// Job er som en cli, med kun et entrypoint og en retur vaerdi...

// CLI -> Help (no start) Foo-> New App