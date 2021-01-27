package io.niochatroom;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * 客户端实现类(nio)
 *
 * @author liangbingtian
 * @date 2021/01/25 下午11:09
 */
public class ChatClient {

  private static final String DEFAULT_SERVER_HOST = "127.0.0.1";
  private static final int DEFAULT_SERVER_PORT = 8888;
  private static final String QUIT = "quit";
  private static final int BUFFER = 1024;
  private String host;
  private int port;
  private SocketChannel client;
  private final ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
  private final ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
  private Selector selector;
  private final Charset charset = StandardCharsets.UTF_8;

  public ChatClient() {
    this.host = DEFAULT_SERVER_HOST;
    this.port = DEFAULT_SERVER_PORT;
  }

  public ChatClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public Boolean readyToQuit(String msg) {
    return msg.equals(QUIT);
  }

  public void close(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * 发送消息
   *
   * @param msg
   */
  public void send(String msg) throws IOException {
    if (msg.isEmpty()) {
      return;
    }
    wBuffer.clear();
    wBuffer.put(charset.encode("客户端" + client.socket().getPort() + ":" + msg));
    wBuffer.flip();
    while (wBuffer.hasRemaining()) {
      client.write(wBuffer);
    }
    if (readyToQuit(msg)){
      close(selector);
    }
  }

  public static void main(String[] args) {
    ChatClient chatClient = new ChatClient("127.0.0.1", DEFAULT_SERVER_PORT);
    chatClient.start();
  }

  private void start() {
    try {
      client = SocketChannel.open();
      client.configureBlocking(false);

      selector = Selector.open();
      client.register(selector, SelectionKey.OP_CONNECT);
      client.connect(new InetSocketAddress(host, port));
      while (true) {
        selector.select();
        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          SelectionKey selectionKey = iterator.next();
          handle(selectionKey);
          iterator.remove();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }finally {
      close(selector);
    }
  }

  private void handle(SelectionKey selectionKey) throws IOException {
    //客户端已经链接了
    if (selectionKey.isConnectable()) {
      SocketChannel client = (SocketChannel) selectionKey.channel();
      if (client.isConnectionPending()) {
        client.finishConnect();
        new Thread(new UserInputHandler(this)).start();
      }
      client.register(selector, SelectionKey.OP_READ);
    }
    //Read事件，服务器转发消息
    if (selectionKey.isReadable()) {
      SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
      String msg = receive(socketChannel);
      if (msg.isEmpty()) {
        close(selector);
      } else {
        System.out.println(msg);
      }
    }
  }

  private String receive(SocketChannel socketChannel) throws IOException {
    rBuffer.clear();
    while (socketChannel.read(rBuffer) > 0) {
      ;
    }
    rBuffer.flip();
    return String.valueOf(charset.decode(rBuffer));
  }
}
