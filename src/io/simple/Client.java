package io.simple;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * 网络通信调用客户端
 *
 * @author liangbingtian
 * @date 2021/01/14 5:35 下午
 */
public class Client {

  public static void main(String[] args) {
    final String DEFAULT_HOST = "127.0.0.1";
    final int DEFAULT_PORT = 8888;
    final String QUIT = "quit";
    Socket socket = null;
    BufferedWriter bufferedWriter = null;
    try {
      socket = new Socket(DEFAULT_HOST, DEFAULT_PORT);
      bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (true) {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));
        String msg = consoleReader.readLine();
        if (QUIT.equals(msg)) {
          System.out.println("关闭Socket");
          break;
        }
        bufferedWriter.write(msg + "\n");
        bufferedWriter.flush();

        System.out.println(bufferedReader.readLine());
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (bufferedWriter != null) {
        try {
          bufferedWriter.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
