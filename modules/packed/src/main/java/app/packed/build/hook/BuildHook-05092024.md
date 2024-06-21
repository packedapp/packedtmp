Build Hooks are the AOP of building an application.

# Supported Component Types (Sealed for now)
Application, Assembly, Container, Bean, Operation


# Mirrors
A bit complicated, do we want to main build hooks reordering in the mirror??? Maybe just the actual class

BuildHookMirror -> Represents a BuildHook instance. A build hook is declared somewhere either on an Assembly/Bean or dynamically. 

AppliedBuildHookMirror -> A build 
Vi har 2 typer mirrors.
Et der angiver (DeclareddBuildMirrors)