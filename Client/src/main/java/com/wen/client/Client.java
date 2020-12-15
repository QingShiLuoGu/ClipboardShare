package com.wen.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Author: wenxl
 * Date: 20-12-15 上午11:02
 * Description:
 */
public class Client {
    public static final String PREFIX = "[_clipboard_start_]";
    public static final String SUFFIX = "[_clipboard_end_]";

    private volatile Socket socket = null;
    private volatile BufferedReader socketIn = null;
    private volatile BufferedWriter socketOut = null;
    public static final String SERVER = "192.168.7.202";
    public static final int PORT = 63521;
    private CallBack callBack = null;

    public Client(CallBack callBack) {
        this.callBack = callBack;
    }

    private void connectServer() {
        System.out.println("client reset()");

        if (socketIn != null) {
            return;
        }
        InetAddress address = null;
        try {
            address = InetAddress.getByName(SERVER);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            socket = new Socket(address, PORT);
            socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socketOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            reset();
        }

    }

    private boolean checkConnect() {
        if (socketOut == null) {
            return false;
        }
        try {
            socketOut.write("test");
            socketOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void reset() {
        System.out.println("client reset()");
        try {
            if (socketOut != null) socketOut.close();
            if (socketIn != null) socketIn.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketIn = null;
        socketOut = null;
        socket = null;
    }

    public void startRead() {
        System.out.println("client startRead()");
        new Thread(new Runnable() {
            @Override
            public void run() {
                String line = null;
                while (true) {
                    while (socket == null || !checkConnect()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        reset();
                        connectServer();
                    }
                    System.out.println("client is ready");
                    try {
                        if ((line = socketIn.readLine()) != null
                                && callBack != null) {
                            callBack.onNewLineCame(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        reset();
                    }
                }
            }
        }).start();
    }

    public interface CallBack {
        void onNewLineCame(String text);
    }

}
