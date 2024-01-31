package io.github.spencerpark.jupyter.messages.debug.bodies;

import com.google.gson.annotations.SerializedName;
import io.github.spencerpark.jupyter.messages.debug.model.DapBreakpoint;

import java.util.List;

// The kernel model in Jupyter separates the client session from the kernel, so a kernel/debugger can already be running
// and then connected to later. The debugInfo command brings the client up to date with the state of the debugger.
public class DebugInfoBody {
    public static class BreakpointsList {
        @SerializedName("source")
        protected final String source;

        @SerializedName("breakpoints")
        protected final List<DapBreakpoint> breakpoints;

        public BreakpointsList(String source, List<DapBreakpoint> breakpoints) {
            this.source = source;
            this.breakpoints = breakpoints;
        }

        public String getSource() {
            return source;
        }

        public List<DapBreakpoint> getBreakpoints() {
            return breakpoints;
        }
    }

    // true if debugger is started
    @SerializedName("isStarted")
    protected final boolean isStarted;

    // default: "Murmur2"
    @SerializedName("hashMethod")
    protected final String hashMethod;

    @SerializedName("hashSeed")
    protected final String hashSeed;

    @SerializedName("tmpFilePrefix")
    protected final String tmpFilePrefix;

    @SerializedName("tmpFileSuffix")
    protected final String tmpFileSuffix;

    @SerializedName("breakpoints")
    protected final List<BreakpointsList> breakpoints;

    @SerializedName("stoppedThreads")
    protected final List<Integer> stoppedThreads;

    @SerializedName("richRendering")
    protected final boolean richRendering;

    @SerializedName("exceptionPaths")
    protected final List<String> exceptionPaths;

    public DebugInfoBody(boolean isStarted, String hashMethod, String hashSeed, String tmpFilePrefix, String tmpFileSuffix, List<BreakpointsList> breakpoints, List<Integer> stoppedThreads, boolean richRendering, List<String> exceptionPaths) {
        this.isStarted = isStarted;
        this.hashMethod = hashMethod;
        this.hashSeed = hashSeed;
        this.tmpFilePrefix = tmpFilePrefix;
        this.tmpFileSuffix = tmpFileSuffix;
        this.breakpoints = breakpoints;
        this.stoppedThreads = stoppedThreads;
        this.richRendering = richRendering;
        this.exceptionPaths = exceptionPaths;
    }
}
