package io.biochatroom.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 用户输入的处理handler
 *
 * @author liangbingtian
 * @date 2021/01/18 下午4:01
 */
public class UserInputHandler implements Runnable{

  private final ChatClient chatClient;

  public UserInputHandler(ChatClient chatClient) {
    this.chatClient = chatClient;
  }


  @Override
  public void run() {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
      while (true) {
        String msg = bufferedReader.readLine();

        chatClient.send(msg);

        if (chatClient.readyToQuit(msg)) {
          break;
        }

      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
