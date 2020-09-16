package com.example.ticketmachinedemo.service;

import com.example.ticketmachinedemo.utils.ByteUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Calendar;

/**
 * @author: 苏敏
 * @date: 2020/9/11 12:09
 */
@Service
public class TicketMachineService {

    private int port = 8082;
    private ServerSocket serverSocket;
    private Socket socket;
    private OutputStream out;

    private Thread connectThread;
    private Thread detectionThread;
    private Thread readerThread;

    Boolean connected=false;

    long timeMillis = System.currentTimeMillis();
    long time = 90000;
    long delay = 10000;

    public TicketMachineService() throws IOException {
        serverSocket = new ServerSocket(port);
        connectThread = new Thread(this::connect);
        connectThread.start();
    }

    /**
     * 连接
     */
    private void connect() {
        try {
            socket = serverSocket.accept();
            socket.setKeepAlive(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("已连接");
        connected=true;
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 开启读取线程
        readerThread = new Thread(this::readResponse);
        readerThread.start();
        // 开启心跳检测线程
        detectionThread = new Thread(this::detection);
        detectionThread.start();
        // 关闭当前连接线程
        connectThread.stop();
    }

    private void detection() {
        while (true) {
            try {
                Thread.sleep(10000);
                if (System.currentTimeMillis() - timeMillis > (time+delay)) {
                    System.out.println(String.format("检测超时，重新连接，毫秒数%d",System.currentTimeMillis() - timeMillis));
                    connected=false;
                    connectThread = new Thread(this::reconnect);
                    connectThread.start();
                    detectionThread.stop();
                    readerThread.stop();
                } else {
                    System.out.println(String.format("检测正常，毫秒数%d",System.currentTimeMillis() - timeMillis));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打开指定箱
     *
     * @param number
     * @throws IOException
     */
    public void openBox(Integer number) throws IOException {
        if (socket == null || socket.isClosed()) {
            System.out.println("兑奖机未连接");
            return;
        }
        String n = Integer.toHexString(number);
        n = n.length() == 1 ? n = "0" + n : n;
        String code = "01710010010500" + n + ByteUtils.getDate() + "0d0a";
        byte[] bytes = ByteUtils.HexString2Byte(code);
        System.out.println(String.format("发送开箱请求%s", n));
        out.write(bytes, 0, bytes.length);
    }

    /**
     * 打开列表
     *
     * @throws IOException
     */
    public void openBoxList(String[] boxNumberList) throws IOException, InterruptedException {
        if (socket.isClosed()) {
            System.out.println("兑奖机未连接");
            return;
        }
        for (String s : boxNumberList) {
            Thread.sleep(2000); // 休眠3秒
            String n = Integer.toHexString(Integer.parseInt(s));
            n = n.length() == 1 ? n = "0" + n : n;
            String code = "01710010010500" + n + ByteUtils.getDate() + "0d0a";
            byte[] bytes = ByteUtils.HexString2Byte(code);
            System.out.println(String.format("发送开箱请求%s", n));
            out.write(bytes, 0, bytes.length);
        }

    }

    /**
     * 打开所有箱
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public void openAll() throws InterruptedException, IOException {
        if (socket == null) {
            System.out.println("兑奖机未连接");
            return;
        }
        for (int i = 1; i < 34; i++) {
            Thread.sleep(2000); // 休眠3秒
            openBox(i);
        }
    }

    /**
     * 接受帧
     */
    private void readResponse() {
        try {
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] buffer = new byte[1024];
            int n;
            while ((n = in.read(buffer)) > 0) {
                System.out.println(String.format("距离上一次请求%d毫秒", System.currentTimeMillis() - timeMillis));
                timeMillis = System.currentTimeMillis();

                String s = ByteUtils.byteArrToHex(buffer);
                if (s.substring(2, 4).equals("70")) {
                    sys(s);
                    String response = "0170001001010000" + s.substring(896, 912);
                    System.out.println(String.format("返回帧%s", response));
                    out.write(ByteUtils.hexStringToByte(response), 0, ByteUtils.hexStringToByte(response).length);
                }
                if (s.substring(2, 4).equals("20")) {
                    sys(s);
                    String response = "0120001001010001" + s.substring(896, 912);
                    System.out.println(String.format("返回帧%s", response));
                    out.write(ByteUtils.hexStringToByte(response), 0, ByteUtils.hexStringToByte(response).length);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void reconnect() {
        System.out.println("重启等待连接");
        try {
//            serverSocket = new ServerSocket(port);
            socket = serverSocket.accept();
            socket.setKeepAlive(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connected=true;
        System.out.println("重启已连接");
        try {
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 开启检测线程
        readerThread = new Thread(this::readResponse);
        readerThread.start();
        detectionThread = new Thread(this::detection);
        detectionThread.start();
        connectThread.stop();
    }

    /**
     * 分析帧
     *
     * @param s
     */
    private void sys(String s) {
        int index = 0;
        // 基本数据
        String cardType = s.substring(index, index += 2);
        String frameType = s.substring(index, index += 2);
        Long length = Long.parseLong(s.substring(index, index += 4), 16);
        String equipmentCode = s.substring(index, index += 30);
        String softwareVersion = s.substring(index, index += 30);
        System.out.println(String.format("板卡：%s,帧类型：%s,帧长度：%d,设备唯一码：%s,软件版本号：%s", cardType, frameType, length, equipmentCode, softwareVersion));

        // 货道数量
        long goodsNumber = Long.parseLong(s.substring(index, index += 2), 16);
        String goodsStates = s.substring(index, index += goodsNumber * 2);
        String goodsRepertory = s.substring(index, index += goodsNumber * 2);
        String goodsPrice = s.substring(index, index += goodsNumber * 4);
        System.out.println(String.format("货道数量：%d，货状态：%s,货库存：%s,货价格：%s", goodsNumber, goodsStates, goodsRepertory, goodsPrice));


        // 纸币机状态
        s.substring(index, index += 2);

        // 硬币机状态
        s.substring(index, index += 2);

        // 温度状态
        long l = Long.parseLong(s.substring(index, index += 2), 16);

        // 扩展版状态
        s.substring(index, index += 2);

        // 保留
        s.substring(index, index += 2);

        // 纸币数量
        s.substring(index, index += 2);

        // 硬币数量
        s.substring(index, index += 2);

        // 网络信号
        s.substring(index, index += 2);

        // 机器类型
        s.substring(index, index += 2);

        // 特殊功能字
        s.substring(index, index += 2);

        // 温区1
        s.substring(index, index += 2);

        // 温区2
        s.substring(index, index += 2);

        // 温区3
        s.substring(index, index += 2);

        // 时间戳
        s.substring(index, index += 12);

//        // 帧尾
//        System.out.println(s.substring(index, index += 4));
    }
}
