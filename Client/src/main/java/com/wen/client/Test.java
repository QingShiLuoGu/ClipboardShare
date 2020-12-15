package com.wen.client;

/**
 * Author: wenxl
 * Date: 20-12-15 下午1:48
 * Description:
 */
class Test {
    public static void main(String[] args) {
        Client client = new Client(new ProtoProcess(new ProtoProcess.CallBack() {
            @Override
            public void onServerClipBoardChanged(String text) {
                System.out.println("onServerClipBoardChanged " + text);
            }
        }));
        client.startRead();
    }
}
