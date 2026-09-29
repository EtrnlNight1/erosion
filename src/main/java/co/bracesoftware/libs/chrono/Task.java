package co.bracesoftware.libs.chrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Task
{
    private static final Queue<Task> PENDING = new ConcurrentLinkedQueue<>();
    private static final List<Task> ACTIVE = new ArrayList<>();

    private int delay;
    private final Runnable task;

    public Task(int d, Runnable t)
    {
        this.delay = d;
        this.task = t;
    }

    public final void complete()
    {
        this.delay = 0;
        this.task.run();
    }

    public final boolean isCompleted()
    {
        return this.delay <= 0;
    }

    public final int getDelay()
    {
        return this.delay;
    }

    public final void weAreAlmostThere()
    {
        --this.delay;
    }

    public static final void schedule(int d, Runnable t)
    {
        PENDING.add(new Task(d,t));
        return;
    }

    //call dis on every tick
    public static final void processPending()
    {
        Task ptask;
        while((ptask = PENDING.poll()) != null)
        {
            ACTIVE.add(ptask);
        }

        for(int i = 0; i < ACTIVE.size(); i++)
        {
            var t = ACTIVE.get(i);
            if(t.isCompleted()) continue;
            t.weAreAlmostThere();
            if(t.getDelay() <= 0) t.complete();
        }

        ACTIVE.removeIf(Task::isCompleted);
        return;
    }

    public static final int getActiveTasks()
    {
        return ACTIVE.size();
    }

    public static final void clearTasks()
    {
        ACTIVE.clear();
        PENDING.clear();
    }
}