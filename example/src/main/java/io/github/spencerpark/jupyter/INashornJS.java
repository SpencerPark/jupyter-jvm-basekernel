package io.github.spencerpark.jupyter;

import io.github.spencerpark.jupyter.api.KernelConnectionProperties;
import io.github.spencerpark.jupyter.channels.JupyterConnection;
import io.github.spencerpark.jupyter.channels.JupyterSocket;
import io.github.spencerpark.jupyter.kernel.ZmqKernelConnector;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class INashornJS {
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new IllegalArgumentException("Missing connection file argument");

        Path connectionFile = Paths.get(args[0]);

        if (!Files.isRegularFile(connectionFile))
            throw new IllegalArgumentException("Connection file '" + connectionFile + "' isn't a file.");

        String contents = new String(Files.readAllBytes(connectionFile), StandardCharsets.UTF_8);

        JupyterSocket.JUPYTER_LOGGER.setLevel(Level.WARNING);

        KernelConnectionProperties connProps = KernelConnectionProperties.parse(contents);
        JupyterConnection connection = new JupyterConnection(connProps);

        String envEngineArgs = System.getenv("JS_ENGINE_ARGS");
        if (envEngineArgs == null)
            envEngineArgs = "-scripting";

        String[] engineArgs = envEngineArgs.split(" ");

        NashornKernel kernel = new NashornKernel(engineArgs);
        ZmqKernelConnector connector = new ZmqKernelConnector(kernel, connection);
        connector.connectKernelTo(connection);

        connection.connect();
        connection.waitUntilClose();
    }
}
