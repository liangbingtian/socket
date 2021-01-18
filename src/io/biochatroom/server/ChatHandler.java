package io.biochatroom.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * bio处理聊天室客户端的handler类
 *
 * @author liangbingtian
 * @date 2021/01/17 下午10:43
 */
public class ChatHandler implements Runnable{

  private final ChatServer chatServer;
  private final Socket socket;

  public ChatHandler(ChatServer chatServer, Socket socket) {
    this.chatServer = chatServer;
    this.socket = socket;
  }


  @Override
  public void run() {
    try {
      chatServer.addClient(socket);
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      String msg = null;
      //阻塞式读取，除非读到值或者读取完了一行。
      while ((msg = bufferedReader.readLine())!=null) {
        String fwdMsg = "客户端:[" + socket.getPort() + "]发送的消息为:" + msg + "\n";
        chatServer.forwardMessage(socket, fwdMsg);
        if (chatServer.readyToQuit(fwdMsg)) {
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }finally {
      try {
        chatServer.removeClient(socket);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
