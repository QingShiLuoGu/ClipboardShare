package com.wen.server;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Author: wenxl
 * Date: 20-12-15 上午9:46
 * Description:
 */
public class ClipboardServer {
    public static final int DEFAULT_PORT = 63521;
    public static final String PREFIX = "[_clipboard_start_]";
    public static final String SUFFIX = "[_clipboard_end_]";

    public static void main(String[] args) {
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        final Server server = new Server();
        server.startListen();

        ReadClipboard readClipboard = new ReadClipboard();
        readClipboard.setCallback(text -> executor.execute(() -> server.writeMsg(getCombinedString(text))));
        readClipboard.listenClipboard();

        server.setClientStateCallback(() -> server.writeMsg(getCombinedString(readClipboard.getLatestText())));
    }

    public static String getCombinedString(String text) {
        return PREFIX + text + SUFFIX;
    }

    public static class Server {
        int port = DEFAULT_PORT;
        volatile private Socket clientSocket = null;
        private volatile PrintWriter writer = null;
        private ClientStateCallback callback;

        public void startListen() {
            new Thread(() -> {
                try {
                    listen();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        public void setClientStateCallback(ClientStateCallback callback) {
            this.callback = callback;
        }

        public void writeMsg(String text) {
            if (writer != null) {
                writer.println(text);
                writer.flush();
                if (writer.checkError()) {
                    System.out.println("got error,reset");
                    reset();
                }
            }
        }

        private PrintWriter getClient() {
            return writer;
        }

        private void reset() {
            System.out.println("reset socket,re listening");
            writer.close();
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            clientSocket = null;
            writer = null;
        }

        //should create new thread to run this
        private void listen() throws IOException, InterruptedException {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                if (clientSocket == null) {
                    clientSocket = serverSocket.accept();
                    writer = new PrintWriter(clientSocket.getOutputStream());
                    if (callback != null) {
                        callback.onClientConnected();
                    }
                    System.out.println("got a client");
                }
                Thread.sleep(1000);
            }
        }

        public interface ClientStateCallback {
            void onClientConnected();
        }
    }

    public static class ReadClipboard implements ClipboardOwner {
        private final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        volatile private String latestText = null;
        private CallBack callback = null;

        public void listenClipboard() {
            clipboard.setContents(clipboard.getContents(null), this);
        }

        public String getLatestText() {
            return latestText;
        }

        public void setCallback(CallBack callback) {
            this.callback = callback;
        }

        @Override
        public void lostOwnership(Clipboard clipboard, Transferable contents) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                try {
                    latestText = (String) clipboard.getData(DataFlavor.stringFlavor);
                } catch (UnsupportedFlavorException | IOException e) {
                    e.printStackTrace();
                }
                if (latestText != null && callback != null) {
                    callback.onClipBoardTextChanged(latestText);
                }
            }

            System.out.println("notified new action: " + latestText);
            clipboard.setContents(clipboard.getContents(null), this);
        }

        interface CallBack {
            void onClipBoardTextChanged(String text);
        }
    }
}
