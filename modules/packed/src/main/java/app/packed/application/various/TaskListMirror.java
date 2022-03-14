package app.packed.application.various;

import app.packed.application.various.TaskListMirror.TaskMirror;

// Maaske vi har to
// TaskListMirror <: SerialTaskListMirror ParallelTaskListMirror

// Just because something is ParallelTaskListMirror does not mean the execution is parallel

// Det er vel bare en liste af operationer....
// Saa det kan genbruges

public interface TaskListMirror extends Iterable<TaskMirror> {

    public void printAll();

    // Ved ikke om der er forskel paa almindelige tasks
    // og "lifecycle tasks"

    // Det er vel bare en operation????

    // OperationList

    ////

    // Ordered lifecycle tasks... Altsaa tasks med en foer/efter
    public interface TaskMirror {

    }

}
