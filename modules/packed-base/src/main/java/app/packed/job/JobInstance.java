package app.packed.job;


// JobInstance er et aktual running job...
// Hvor Job representere et fremtidigt job, evt endda scheduleret
public interface JobInstance {

    Task task(); // the main task...
}


