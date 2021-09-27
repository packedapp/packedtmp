package zandbox.internal.hooks2.repository;

import java.lang.annotation.Annotation;

import zandbox.internal.hooks2.bootstrap.AbstractBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleFieldBootstrapModel;
import zandbox.internal.hooks2.bootstrap.AccessibleMethodBootstrapModel;
import zandbox.internal.hooks2.bootstrap.InjectableVariableBootstrapModel;
import zandbox.packed.hooks.AccessibleFieldHook;
import zandbox.packed.hooks.AccessibleMethodHook;
import zandbox.packed.hooks.InjectAnnotatedVariableHook;

public final class MetaHookRepository extends AbstractCachedHookRepository {

    @Override
    protected AccessibleFieldBootstrapModel newFieldHookModel(Class<? extends Annotation> annotation, AccessibleFieldHook fh) {
        return AccessibleFieldBootstrapModel.of(fh);
    }

    @Override
    protected AbstractBootstrapModel newMethodHookModel(Class<? extends Annotation> type, AccessibleMethodHook fh) {
        return AccessibleMethodBootstrapModel.of(fh);
    }

    @Override
    protected InjectableVariableBootstrapModel newInjectableVaritableHookModel(Class<? extends Annotation> annotation, InjectAnnotatedVariableHook ivh) {
        return InjectableVariableBootstrapModel.of(ivh);
    }
}
