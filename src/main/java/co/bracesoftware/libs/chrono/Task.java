package co.bracesoftware.libs.chrono;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Task
{
    public static final class Async
    {
        public static final CompletableFuture<?> async(Runnable t)
        {
            return CompletableFuture.runAsync(t).exceptionally(
                e -> {
                    System.out.println(e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            );
        }

        public static final Task schedule(int d, Runnable t)
        {
            return skedule(d, t, true);
        }
    }

    private static final Queue<Task> PENDING = new ConcurrentLinkedQueue<>();
    private static final List<Task> ACTIVE = new ArrayList<>();
    private CompletableFuture<?> future;

    private int delay;
    private final Runnable task;
    private final boolean async;

    Task(int d, Runnable t, boolean a)
    {
        this.delay = d;
        this.task = t;
        this.async = a;
    }

    public final void complete()
    {
        this.delay = 0;

        if(this.async) this.future = Async.async(this.task);
        else this.task.run();
    }

    public final boolean isCompleted()
    {
        if(this.async) return this.future != null && this.future.isDone();
        return this.isDispatched();
    }

    public final boolean isDispatched()
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

    public static final Task schedule(int d, Runnable t)
    {
        return skedule(d,t,false);
    }

    public static final Task skedule(int d, Runnable t, boolean a)
    {
        var ta = new Task(d,t,a);
        PENDING.add(ta);
        return ta;
    }

    //call dis on every tick
    public static final void processPending()
    {
        Task ptask;
        while((ptask = PENDING.poll()) != null)
        {
            ACTIVE.add(ptask);
        }

        for(int i = ACTIVE.size() - 1; i >= 0; i--)
        {
            var t = ACTIVE.get(i);
            t.weAreAlmostThere();

            if(t.getDelay() <= 0)
            {
                t.complete();

                int li = ACTIVE.size() - 1;
                ACTIVE.set(i, ACTIVE.get(li));
                ACTIVE.remove(li);
            }
        }
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