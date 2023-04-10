package app.packed.cli;

import sandbox.extension.operation.OperationHandle;

record PackedCliCommand(CliExtensionNamespace namespace, CliCommand command, OperationHandle operation) {}