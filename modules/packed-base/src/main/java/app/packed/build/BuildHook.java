package app.packed.build;

import app.packed.bean.hooks.BeanBuildHook;
import app.packed.container.sandbox.AssemblyBuildHook;

public sealed interface BuildHook permits AssemblyBuildHook,BeanBuildHook {

}
