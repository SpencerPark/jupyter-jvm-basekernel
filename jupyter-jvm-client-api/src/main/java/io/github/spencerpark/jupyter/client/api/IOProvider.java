package io.github.spencerpark.jupyter.client.api;

import io.github.spencerpark.jupyter.api.display.DisplayData;
import io.github.spencerpark.jupyter.api.display.mime.MIMEType;

public interface IOProvider {
    public static final IOProvider NULL = new IOProvider() {
        @Override
        public void writeOut(String data) { }

        @Override
        public void writeErr(String data) { }

        @Override
        public boolean supportsStdin() {
            return false;
        }

        @Override
        public String readIn(String prompt, boolean isPassword) {
            return null;
        }

        @Override
        public void writeDisplay(DisplayData data) { }

        @Override
        public void updateDisplay(String id, DisplayData data) { }

        @Override
        public void clear(boolean defer) { }
    };

    public static final IOProvider STDIO = new IOProvider() {
        @Override
        public void writeOut(String data) {
            System.out.print(data);
        }

        @Override
        public void writeErr(String data) {
            System.err.print(data);
        }

        @Override
        public boolean supportsStdin() {
            return true;
        }

        @Override
        public String readIn(String prompt, boolean isPassword) {
            System.out.print(prompt + ": ");
            if (!isPassword)
                return System.console().readLine();

            char[] password = System.console().readPassword();
            return new String(password);
        }

        @Override
        public void writeDisplay(DisplayData data) {
            System.out.println(data.getData(MIMEType.TEXT_PLAIN));
        }

        @Override
        public void updateDisplay(String id, DisplayData data) {
            this.writeDisplay(data);
        }

        @Override
        public synchronized void clear(boolean defer) {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    };

    public void writeOut(String data);
    public void writeErr(String data);

    public boolean supportsStdin();
    public String readIn(String prompt, boolean isPassword);

    public void writeDisplay(DisplayData data);
    public void updateDisplay(String id, DisplayData data);

    public void clear(boolean defer);
}
