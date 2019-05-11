package io.github.spencerpark.jupyter.client.channels;

import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.channels.Loop;
import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.messages.HMACGenerator;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HeartbeatClientChannel extends JupyterSocket {
    public static long DEFAULT_MS_UNTIL_CONSIDERED_DEAD = 1000;
    public static int DEFAULT_ALLOWED_FAILURES_UNTIL_CONSIDERED_DEAD = 3;

    public enum State {
        BEATING(true, true),
        WAITING_ON_ECHO(true, true),
        MESSAGING_FAILURE(false, true),
        PAUSED(false, true),
        DEAD(false, false);

        private final boolean beating;
        private final boolean recoverable;

        State(boolean beating, boolean recoverable) {
            this.beating = beating;
            this.recoverable = recoverable;
        }

        public boolean isStrong() {
            return this.beating;
        }

        public boolean isWeak() {
            return this.recoverable;
        }

        public boolean isDead() {
            return !this.beating && !this.recoverable;
        }
    }

    private static final AtomicInteger HEARTBEAT_ID = new AtomicInteger();

    private volatile Loop pulse;

    private final int numAllowedFailuresBeforeConsideredDead;
    private final long timeUntilConsideredDead;
    private Runnable onDeath;

    private AtomicInteger consecutiveFailureCount = new AtomicInteger(0);
    private AtomicReference<State> state = new AtomicReference<>(State.PAUSED);

    public HeartbeatClientChannel(ZMQ.Context context, HMACGenerator hmacGenerator) {
        this(context, hmacGenerator, DEFAULT_ALLOWED_FAILURES_UNTIL_CONSIDERED_DEAD, DEFAULT_MS_UNTIL_CONSIDERED_DEAD);
    }

    public HeartbeatClientChannel(ZMQ.Context context, HMACGenerator hmacGenerator, int numAllowedFailuresBeforeConsideredDead, long msUntilConsideredDead) {
        super(context, SocketType.REQ, hmacGenerator, Logger.getLogger("HeartbeatChannel-client"));
        this.numAllowedFailuresBeforeConsideredDead = numAllowedFailuresBeforeConsideredDead;
        this.timeUntilConsideredDead = msUntilConsideredDead;
    }

    public boolean isStrong() {
        return this.state.get().isStrong();
    }

    public boolean isWeak() {
        return this.state.get().isWeak();
    }

    public boolean isDead() {
        return this.state.get().isDead();
    }

    public boolean isPaused() {
        return this.state.get() == State.PAUSED;
    }

    public void setPaused(boolean paused) {
        this.state.updateAndGet(state ->
                state.isDead()
                        ? State.DEAD
                        : paused
                                ? State.PAUSED
                                : State.BEATING
        );
    }

    public void onDeath(Runnable onDeath) {
        if (this.onDeath != null) {
            Runnable oldCallback = this.onDeath;
            this.onDeath = () -> {
                oldCallback.run();
                onDeath.run();
            };
        } else {
            this.onDeath = onDeath;
        }
    }


    private boolean isBound() {
        return this.pulse != null;
    }

    private void setDead() {
        this.state.set(State.DEAD);
        if (this.onDeath != null)
            this.onDeath.run();
    }

    private void countFailure() {
        this.state.set(State.MESSAGING_FAILURE);
        int numFailures = this.consecutiveFailureCount.incrementAndGet();
        if (numFailures >= this.numAllowedFailuresBeforeConsideredDead)
            this.setDead();
    }

    private void setState(State state) {
        switch (state) {
            case DEAD:
                this.setDead();
                return;
            case PAUSED:
                this.state.set(State.PAUSED);
                return;
            case MESSAGING_FAILURE:
                this.countFailure();
                return;
            case WAITING_ON_ECHO:
                this.state.set(State.WAITING_ON_ECHO);
                return;
            case BEATING:
                this.state.set(State.BEATING);
                this.consecutiveFailureCount.set(0);
                return;
        }
    }

    @Override
    public void bind(KernelConnectionProperties connProps) {
        if (this.isBound())
            throw new IllegalStateException("Heartbeat client channel already bound");

        String channelThreadName = "Heartbeat-client-" + HEARTBEAT_ID.getAndIncrement();
        String addr = JupyterSocket.formatAddress(connProps.getTransport(), connProps.getIp(), connProps.getHbPort());

        logger.log(Level.INFO, String.format("Binding %s to %s.", channelThreadName, addr));
        super.connect(addr);

        ZMQ.Poller poller = super.ctx.poller(1);
        poller.register(this, ZMQ.Poller.POLLIN);

        byte[] pingMsg = new byte[4];

        this.pulse = new Loop(channelThreadName, () -> {
            if (this.isPaused())
                // Skip the echo sequence
                return this.timeUntilConsideredDead;

            // Generate and send a random ping message, the canonical client
            // simply sends b'ping' but we can easily do something a bit better
            // in case for some reason messages get backed up?
            ThreadLocalRandom.current().nextBytes(pingMsg);
            if (!super.send(pingMsg)) {
                super.logger.log(Level.SEVERE, "Could not send heartbeat request.");

                this.setState(State.MESSAGING_FAILURE);

                return this.timeUntilConsideredDead;
            }

            long sendTime = System.currentTimeMillis();

            // Wait for the echo response
            this.setState(State.WAITING_ON_ECHO);
            int events = poller.poll(this.timeUntilConsideredDead);
            if (events > 0) {
                byte[] msg = this.recv();
                if (msg == null) {
                    // Error during receive, just continue
                    super.logger.log(Level.SEVERE, "Poll returned 1 event but could not read the echo string.");

                    this.setState(State.MESSAGING_FAILURE);

                    return this.timeUntilConsideredDead;
                }

                if (Arrays.equals(pingMsg, msg))
                    this.setState(State.BEATING);
                else
                    this.setState(State.MESSAGING_FAILURE);

                super.logger.log(Level.FINEST, "Heartbeat pulse");

                // Sleep until the next pulse. This is the frame time less the time elapsed while
                // waiting for the reply.
                return Math.max(0L, this.timeUntilConsideredDead - (System.currentTimeMillis() - sendTime));
            } else {
                // Did not receive a response by the timeout, considered dead.
                this.setState(State.DEAD);
                return -1L;
            }
        });

        this.pulse.onClose(() -> {
            logger.log(Level.INFO, channelThreadName + " shutdown.");
            this.pulse = null;
        });

        this.pulse.start();
        logger.log(Level.INFO, "Beating on " + channelThreadName);
    }

    @Override
    public void close() {
        if (this.isBound())
            this.pulse.shutdown();

        super.close();
    }

    @Override
    public void waitUntilClose() {
        if (this.pulse != null) {
            try {
                this.pulse.join();
            } catch (InterruptedException ignored) { }
        }
    }
}
