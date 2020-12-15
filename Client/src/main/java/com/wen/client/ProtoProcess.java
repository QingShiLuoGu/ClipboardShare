package com.wen.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Author: wenxl
 * Date: 20-12-15 上午11:48
 * Description:
 */
public class ProtoProcess implements Client.CallBack {
    public static final String PREFIX = "[_clipboard_start_]";
    public static final String SUFFIX = "[_clipboard_end_]";

    private CallBack callBack;
    private String lastMsg = null;
    private StringBuilder sb = new StringBuilder();
    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    public ProtoProcess(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public void onNewLineCame(final String text) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                process(text);
            }
        });
    }

    public String getLastMsg() {
        return lastMsg;
    }

    private void process(String text) {
        System.out.println("onNewLineCame " + text);

        int prefixIndex = text.indexOf(PREFIX);
        int suffixIndex = text.indexOf(SUFFIX);
        if (prefixIndex != -1) {
            if (sb.length() != 0) {
                lastMsg = sb.toString();
                sb.delete(0, sb.length());
                if (callBack != null) {
                    callBack.onServerClipBoardChanged(lastMsg);
                }
            }
        }

        sb.append(text, prefixIndex == -1 ? 0 : prefixIndex + PREFIX.length(),
                suffixIndex == -1 ? text.length() : suffixIndex);
        if (suffixIndex != -1) {
            lastMsg = sb.toString();
            sb.delete(0, sb.length());
            if (callBack != null) {
                callBack.onServerClipBoardChanged(lastMsg);
            }
        }
        if (suffixIndex + SUFFIX.length() < text.length()) {
            sb.append(text.substring(suffixIndex + SUFFIX.length()));
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {
        void onServerClipBoardChanged(String text);
    }
}
