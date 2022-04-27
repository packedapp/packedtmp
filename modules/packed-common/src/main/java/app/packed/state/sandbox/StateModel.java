package app.packed.state.sandbox;

// Vi har 2 mulige taenker jeg...

// Vi kender altid alle states... Det ligger fast vi er finite

// Men nogen gange kender vi alle mulige transitioner... 
// Nogen gange goer vi ikke

// Sidechannels/reasons/...
public interface StateModel {

}
/// Hvordan modellere vi restarting...
// Det er jo helt sikkert noget med state at goere
// Men vi kan ikke smide en Restarting state ind over

// Condition ... [Restarting, Resuming, Pausing, Upgrading].. Failing???, Completed, idk

// They are typically boolean...
//// If they where strings... It would be possible to use them from annotations

//// We want some kind of type safety???? Eller vi vil ikke have ransom conditions...
// Men f.eks. Restartable som en kondition

// @OnStart + Restarting
// @OnStop + Restarting

// @OnStart + Restarting + Upgrading
// @OnStop + Restarting + Upgrading

// Upgrading and Restarting


// Vi vil gerne supportere