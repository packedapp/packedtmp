package app.packed.cli;

import app.packed.extension.operation.OperationHandle;

record CliC(CliExtensionDomain domain, CliCommand command, OperationHandle operation) {}