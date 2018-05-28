package io.github.spencerpark.jupyter.kernel.display.common;

import io.github.spencerpark.jupyter.kernel.display.RenderContext;
import io.github.spencerpark.jupyter.kernel.display.Renderer;
import io.github.spencerpark.jupyter.kernel.display.mime.MIMEType;

import javax.sound.sampled.AudioInputStream;

public class Audio {
    private static MIMEType WAV = MIMEType.parse("audio/wav");

    public static void registerAll(Renderer renderer) {
        renderer.createRegistration(AudioInputStream.class)
                .preferring(WAV)
                .register(Audio::renderAIS);
    }

    private static void renderAIS(AudioInputStream stream, RenderContext ctx) {

    }
}
