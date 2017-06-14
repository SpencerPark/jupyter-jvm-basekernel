package io.github.spencerpark.jupyter.channels;

public class Loop extends Thread {
    private boolean running = false;
    private final long sleep;
    private Runnable callback;

    public Loop(String name, long sleep, Runnable target) {
        super(target, name);
        this.sleep = sleep;
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

    @Override
    public void run() {
        while (running) {
            super.run();
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
