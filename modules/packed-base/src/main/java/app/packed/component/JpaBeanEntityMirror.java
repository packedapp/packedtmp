package app.packed.component;

import app.packed.extension.old.ExtensionBeanMirror;

// maaske er for (JpaEntity e : useExtension(JpaEntityExtensionMirror).entities())
interface JpaEntityBeanMirror extends BeanMirror, JpaEntityMirror {

}

interface JpaEntityMirror {
    String tableName();
}

interface JpaRepositoryMirror extends ExtensionBeanMirror { 
    
    // Share denne faetter paa tvaers af alt
    /// HibernateRepo
}
