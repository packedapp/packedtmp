package app.packed.application;

// Maaske vi har to
// TaskListMirror <: SerialTaskListMirror ParallelTaskListMirror

// Just because something is ParallelTaskListMirror does not mean the execution is parallel
public interface TaskListMirror extends Iterable<TaskMirror> {

    public void printAll();
}
