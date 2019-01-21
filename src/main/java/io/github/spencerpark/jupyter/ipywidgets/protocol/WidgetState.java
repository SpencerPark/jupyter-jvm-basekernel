package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonElement;

import java.util.EnumSet;

public interface WidgetState {
    public default StatePatch constructPatch() {
        return this.constructPatch(EnumSet.of(StatePatch.Opts.CLEAR_DIRTY));
    }

    public StatePatch constructPatch(EnumSet<StatePatch.Opts> opts);

    public void applyPatch(StatePatch patch);

    public void handleCustomContent(JsonElement content);
}
