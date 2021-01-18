package io.simple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 网络调用服务端
 *
 * @author liangbingtian
 * @date 2021/01/14 5:35 下午
 */
public class Server {

  public static void main(String[] args) {
    final int DEFAULT_PORT = 8888;
    final String QUIT = "quit";
    ServerSocket serverSocket = null;
    try {
      //绑定监听端口
      serverSocket = new ServerSocket(DEFAULT_PORT);
      System.out.println("启动服务端，监听端口:" + DEFAULT_PORT);
      Socket socket = serverSocket.accept();
      System.out.println("客户端[" + socket.getPort() + "]已经连接");
      BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      String msg = null;
      while ((msg = reader.readLine()) != null) {
        //读取客户端发送的消息
        System.out.println("客户端[" + socket.getPort() + "]发送的消息为:" + msg);
        //回复消息
        writer.write("服务器：" + msg + "\n");
        writer.flush();
        if (QUIT.equals(msg)) {
          System.out.println("客户端["+socket.getPort()+"]已断开连接");
          break;
        }
      }
    }catch (IOException e) {
      e.printStackTrace();
    }finally {
      try {
        if (serverSocket!=null) {
          serverSocket.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
