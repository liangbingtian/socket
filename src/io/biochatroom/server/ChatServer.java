package io.biochatroom.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天室的客户端服务器
 *
 * @author liangbingtian
 * @date 2021/01/15 10:05 上午
 */
public class ChatServer {

  final int DEFAULT_PORT = 8888;
  final String QUIT = "quit";

  ServerSocket serverSocket = null;
  Map<Integer, Writer> connectedClients = null;
  private ExecutorService executorService;

  public static void main(String[] args) {
    ChatServer chatServer = new ChatServer();
    chatServer.start();
  }

  public ChatServer() {
    this.connectedClients = new HashMap<>();
    executorService = Executors.newFixedThreadPool(10);
  }

  public synchronized void addClient(Socket socket) throws IOException {
    int port = socket.getPort();
    BufferedWriter bufferedWriter = new BufferedWriter(
        new OutputStreamWriter(
            socket.getOutputStream()));
    connectedClients.put(port, bufferedWriter);
    System.out.println("客户端["+port+"]已经链接到服务器");
  }

  public synchronized void removeClient(Socket socket) throws IOException {
    if (socket==null) {
      return;
    }
    int port = socket.getPort();
    if (connectedClients.containsKey(port)) {
      connectedClients.get(port).close();
    }
    connectedClients.remove(port);
    System.out.println("客户端["+port+"]已经断开连接");
  }

  public synchronized void forwardMessage(Socket socket, String fwdMsg) throws IOException {
    for (Map.Entry<Integer, Writer> map : connectedClients.entrySet()) {
      Integer key = map.getKey();
      Writer writer = map.getValue();
      if (!key.equals(socket.getPort())) {
        writer.write(fwdMsg);
        writer.flush();
      }
    }
  }

  public void start() {
    try {
      ServerSocket serverSocket = new ServerSocket(DEFAULT_PORT);
      System.out.println("启动服务器，监听端口:"+DEFAULT_PORT+".....");
      while (true) {
        //调用者调用后的状态为阻塞的
        Socket socket = serverSocket.accept();
        //开启线程创建chatHandler
        executorService.execute(new ChatHandler(this, socket));
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close();
    }
  }

  public void close() {
    if (serverSocket!=null) {
      try {
        serverSocket.close();
        System.out.println("关闭了SeverSocket");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public Boolean readyToQuit(String msg) {
    return QUIT.equals(msg);
  }



}
