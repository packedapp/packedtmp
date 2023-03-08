package app.packed.cli;

import sandbox.extension.operation.OperationHandle;

record CliC(CliExtensionDomain domain, CliCommand command, OperationHandle operation) {}