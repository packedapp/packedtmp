package app.packed.cli;

import sandbox.extension.operation.OperationHandle;

record PackedCliCommand(CliExtensionNamespaceOperator namespace, CliCommand command, OperationHandle operation) {}