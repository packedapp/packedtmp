package app.packed.bean.hooks.sandbox;

import app.packed.bean.BeanConfiguration;
import app.packed.bean.BeanMirror;


// Hmmmmm hvad er usecases
public interface BeanBuildHook {
    
    void afterBuild(BeanConfiguration configuration);

    void beforeBuild(BeanConfiguration configuration);

    void verify(BeanMirror container);
}
