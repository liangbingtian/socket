package io.niochatroom;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * 聊天室的客户端服务器(nio)
 *
 * @author liangbingtian
 * @date 2021/01/21 下午11:09
 */
public class ChatServer {

  final int DEFAULT_PORT = 8888;
  final String QUIT = "quit";
  final Integer BUFFER = 1024;

  private Selector selector;
  private final ByteBuffer rBuffer = ByteBuffer.allocate(BUFFER);
  private final ByteBuffer wBuffer = ByteBuffer.allocate(BUFFER);
  private final Charset charset = StandardCharsets.UTF_8;
  private final int port;

  public ChatServer(int port) {
    this.port = port;
  }

  public ChatServer() {
    this.port = 8888;
  }

  private void start() {
    try {
      ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
      serverSocketChannel.configureBlocking(false);
      serverSocketChannel.socket().bind(new InetSocketAddress(DEFAULT_PORT));

      selector = Selector.open();
      serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
      System.out.println("启动服务器，监听端口:"+port+"...");

      while (true) {
        selector.select();
        Set<SelectionKey> keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()) {
          handle(iterator.next());
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
    //ACCEPT 事件，和客户端建立了链接
    if (selectionKey.isAcceptable()) {
      SocketChannel client = (SocketChannel) selectionKey.channel();
      client.configureBlocking(false);
      client.register(selector, SelectionKey.OP_READ);
      System.out.println(getClientName(client) + "已经链接..");
    }else if (selectionKey.isReadable()) {
      SocketChannel client = (SocketChannel) selectionKey.channel();
      String fwdMsg = receive(client);
      if (fwdMsg.isEmpty()) {
        selectionKey.channel();
        selector.wakeup();
      }else {
        forwardMsg(client, fwdMsg);

        //检查用户是否退出
        if (readyToQuit(fwdMsg)) {
          selectionKey.channel();
          selector.wakeup();
          System.out.println(getClientName(client) + "已经断开");
        }
      }
    }
  }

  private void forwardMsg(SocketChannel client, String fwdMsg) throws IOException {
    for (SelectionKey key : selector.keys()) {
      Channel connectedChannel =  key.channel();
      if (connectedChannel instanceof ServerSocketChannel) {
        continue;
      }
      if (key.isValid() && !client.equals(connectedChannel)) {
        wBuffer.clear();
        wBuffer.put(charset.encode(getClientName(client.socket().getChannel())+":"+fwdMsg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
          ((SocketChannel) connectedChannel).write(wBuffer);
        }
      }
    }
  }

  private String getClientName(SocketChannel client) {
    return "客户端[" + client.socket().getPort()+"]";
  }

  private String receive(SocketChannel socketChannel) throws IOException {
    rBuffer.clear();
    while ((socketChannel.read(rBuffer))>0);
    rBuffer.flip();
    return String.valueOf(charset.decode(rBuffer));
  }

  public static void close(Closeable closeable) {
    try {
      if (closeable!=null) {
        closeable.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public Boolean readyToQuit(String msg) {
    return QUIT.equals(msg);
  }

  public static void main(String[] args) {
    ChatServer chatServer = new ChatServer(7777);
    chatServer.start();
  }

}
