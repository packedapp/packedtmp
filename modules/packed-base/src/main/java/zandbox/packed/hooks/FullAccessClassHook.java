package zandbox.packed.hooks;

// Hvordan relatere den til en ComponentDriver????

// F.eks
// @Request <--- er det i virkeligheden et class hook??? Man kan sige @Entity er det jo...
// Men @Entity vil vi gerne installere specielt jo... Fordi vi gerne vil have de alle sammen har et repository parent component node...

// Component classes annotated with @Entity must be installed via JpaRepository#install

// Det kan altsaa ogsaa vaere man skal benytte en driver til det...
// Kan ikke forstille mig en model... Hvor man skal dele accesses
public class FullAccessClassHook {}

// Man skal angive en driver. og en Binder?

class FullAccessClassHookSpecialBeanDriver {}

// Uhh, er begyndt lidt at se sammenhaengen mellem et class hook og en bean driver???

// @AssistedInject <-- det er jo paa en metode...
//// Det er vel egentlig ikke en hook??? Eller er det IDK.
//// Men den kan ihvertfald ikke bruges standalone...

//// AssistedInject... er en ClassGenBean