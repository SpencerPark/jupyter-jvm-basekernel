package io.github.spencerpark.jupyter.ipywidgets.protocol;

import com.google.gson.JsonElement;

import java.util.EnumSet;

public interface WidgetState {
    public default StatePatch createPatch() {
        return this.createPatch(EnumSet.of(StatePatch.Opts.CLEAR_DIRTY));
    }

    public StatePatch createPatch(EnumSet<StatePatch.Opts> opts);

    public void applyPatch(StatePatch patch);

    public void handleCustomContent(JsonElement content);
}
