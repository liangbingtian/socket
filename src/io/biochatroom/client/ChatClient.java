package io.biochatroom.client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 聊天室客户端
 *
 * @author liangbingtian
 * @date 2021/01/18 下午3:23
 */
public class ChatClient {

  private static final String DEFAULT_SEVER_HOST = "127.0.0.1";
  private static final Integer DEFAULT_SEVER_PORT = 8888;
  private static final String QUIT = "QUIT";

  private BufferedReader bufferedReader;

  private BufferedWriter bufferedWriter;

  private Socket socket;

  /**
   * 发送消息
   * @param msg
   */
  public void send(String msg) throws IOException {
    if (!socket.isOutputShutdown()) {
      bufferedWriter.write(msg + "\n");
      bufferedWriter.flush();
    }
  }

  /**
   * 接收消息
   */
  public String receive() throws IOException {
    if (!socket.isInputShutdown()) {
      return bufferedReader.readLine();
    }
    return null;
  }

  public Boolean readyToQuit(String msg) {
    return QUIT.equals(msg);
  }

  public void close() {
    if (bufferedWriter!=null) {
      try {
        System.out.println("关闭socket");
        bufferedWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void start() {
    try {
      Socket socket = new Socket(DEFAULT_SEVER_HOST, DEFAULT_SEVER_PORT);
      bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

      //处理用户输入
      new Thread(new UserInputHandler(this)).start();
      //读取服务器转发的信息
      String msg = null;
      while ((msg = receive())!=null) {
        System.out.println(msg + "\n");
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close();
    }
  }

  public static void main(String[] args) {
    ChatClient chatClient = new ChatClient();
    chatClient.start();
  }

}
