package app.packed.component;

import app.packed.extension.ExtensorMirror;

// maaske er for (JpaEntity e : useExtension(JpaEntityExtensionMirror).entities())
interface JpaEntityBeanMirror extends BeanMirror, JpaEntityMirror {

}

interface JpaEntityMirror {
    String tableName();
}

interface JpaRepositoryMirror extends ExtensorMirror { 
    
    // Share denne faetter paa tvaers af alt
    /// HibernateRepo
}
