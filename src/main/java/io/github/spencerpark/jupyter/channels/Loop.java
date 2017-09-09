package io.github.spencerpark.jupyter.channels;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;

public class Loop extends Thread {
    private boolean running = false;
    private final long sleep;
    private Runnable callback;
    private final Queue<Runnable> runNextQueue;

    public Loop(String name, long sleep, Runnable target) {
        super(target, name);
        this.sleep = sleep;
        this.runNextQueue = new LinkedList<>();
    }

    public void onClose(Runnable callback) {
        if (this.callback != null) {
            Runnable oldCallback = this.callback;
            this.callback = () -> {
                oldCallback.run();
                callback.run();
            };
        } else {
            this.callback = callback;
        }
    }

    public void doNext(Runnable next) {
        this.runNextQueue.offer(next);
    }

    @Override
    public void run() {
        Runnable next;
        while (running) {
            // Run the loop body
            super.run();

            // Run all queued tasks
            while ((next = runNextQueue.poll()) != null)
                next.run();

            if (sleep > 0) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    running = false;
                }
            }
        }

        if (this.callback != null)
            this.callback.run();
        this.callback = null;
    }

    @Override
    public synchronized void start() {
        super.start();
        running = true;
    }

    public void shutdown() {
        this.running = false;
    }
}
