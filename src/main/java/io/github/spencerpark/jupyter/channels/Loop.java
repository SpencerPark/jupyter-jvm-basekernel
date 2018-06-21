package io.github.spencerpark.jupyter.channels;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class Loop extends Thread {
    private final Logger logger;

    private volatile boolean running = false;
    private final long sleep;
    private Runnable callback;
    private final Queue<Runnable> runNextQueue;

    public Loop(String name, long sleep, Runnable target) {
        super(target, name);
        this.sleep = sleep;
        this.runNextQueue = new LinkedBlockingQueue<>();

        this.logger = Logger.getLogger("Loop-" + name);
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
        while (this.running) {
            // Run the loop body
            super.run();

            // Run all queued tasks
            while ((next = this.runNextQueue.poll()) != null)
                next.run();

            if (this.sleep > 0) {
                try {
                    Thread.sleep(this.sleep);
                } catch (InterruptedException e) {
                    this.logger.info("Loop interrupted. Stopping...");
                    this.running = false;
                }
            }
        }

        this.logger.info("Running loop shutdown callback.");

        if (this.callback != null)
            this.callback.run();
        this.callback = null;

        this.logger.info("Loop stopped.");
    }

    @Override
    public synchronized void start() {
        this.logger.info("Loop starting...");

        this.running = true;
        super.start();

        this.logger.info("Loop started.");
    }

    public void shutdown() {
        this.running = false;
        this.logger.info("Loop shutdown.");
    }
}
