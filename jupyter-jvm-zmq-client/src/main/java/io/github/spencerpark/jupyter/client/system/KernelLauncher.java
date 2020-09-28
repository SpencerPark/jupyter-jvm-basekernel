package io.github.spencerpark.jupyter.client.system;

import io.github.spencerpark.jupyter.api.KernelConnectionProperties;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KernelLauncher {
    private static int[] findOpenPorts(int numPorts) {
        int[] ports = new int[numPorts];
        ServerSocket[] dummies = new ServerSocket[numPorts];

        // Open ports with port 0 to let the system find an open one
        for (int i = 0; i < ports.length; i++) {
            try {
                dummies[i] = new ServerSocket(0);
                ports[i] = dummies[i].getLocalPort();
            } catch (IOException ignored) { }
        }

        // Close the dummy sockets
        for (ServerSocket dummy : dummies) {
            try {
                dummy.setReuseAddress(true);
                dummy.close();
            } catch (IOException ignored) { }
        }

        for (int port : ports) {
            if (port == 0)
                throw new IllegalStateException("Could not find " + numPorts + " available ports.");
        }

        return ports;
    }

    // TODO support ipc

    public static KernelConnectionProperties createLocalTcpProps() {
        int[] ports = KernelLauncher.findOpenPorts(5);
        return new KernelConnectionProperties(
                "127.0.0.1",
                ports[0], ports[1], ports[2], ports[3], ports[4],
                "tcp",
                "hmac-sha256",
                null
        );
    }

    public static KernelConnectionProperties createLocalTcpProps(String signatureScheme, String key) {
        int[] ports = KernelLauncher.findOpenPorts(5);
        return new KernelConnectionProperties(
                "127.0.0.1",
                ports[0], ports[1], ports[2], ports[3], ports[4],
                "tcp",
                signatureScheme,
                key
        );
    }

    public static ProcessBuilder prepare(JupyterPaths paths, KernelConnectionProperties props, KernelSpec spec, Path cwd) throws IOException {
        List<Path> runtimeDirs = paths == null ? Collections.emptyList() : paths.getRuntimeDirs();

        Path connectionFile;
        if (runtimeDirs.isEmpty())
            connectionFile = Files.createTempFile("kernel", ".json");
        else
            connectionFile = Files.createTempFile(runtimeDirs.get(0), "kernel", ".json");

        Files.write(connectionFile, props.toJsonString().getBytes(Charset.forName("utf8")));


        List<String> args = spec.getArgv().stream()
                .map(arg -> arg.replace("{connection_file}", connectionFile.toAbsolutePath().toString()))
                .collect(Collectors.toList());

        Map<String, String> env = spec.getEnv();

        ProcessBuilder pb = new ProcessBuilder();
        pb.command(args);
        pb.environment().putAll(env);
        pb.directory(cwd.toFile());

        return pb;
    }

    public static Process launch(JupyterPaths paths, KernelConnectionProperties props, KernelSpec spec, Path cwd) throws IOException {
        return KernelLauncher.prepare(paths, props, spec, cwd)
                .inheritIO()
                .start();
    }
}
