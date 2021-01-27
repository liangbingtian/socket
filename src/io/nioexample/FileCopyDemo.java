package io.nioexample;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 文件拷贝
 *
 * @author liangbingtian
 * @date 2021/01/18 下午11:24
 */
interface FileCopyRunner{
  void copyFile(File source, File target);
}

public class FileCopyDemo {

  public static void close(Closeable closeable) {
    try {
      if (closeable!=null) {
        closeable.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    FileCopyRunner noBufferStreamCopy = (source, target) -> {
      InputStream fileInputStream = null;
      OutputStream fileOutputStream = null;
      try {
        fileInputStream = new FileInputStream(source);
        fileOutputStream = new FileOutputStream(target);
        int result;
        while ((result = fileInputStream.read())!=-1) {
          fileOutputStream.write(result);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        close(fileInputStream);
        close(fileOutputStream);
      }
    };

    FileCopyRunner bufferedStreamCopy = (source, target) -> {
      InputStream inputStream = null;
      OutputStream outputStream = null;
      try {
        inputStream = new BufferedInputStream(new FileInputStream(source));
        outputStream = new BufferedOutputStream(new FileOutputStream(target));
        byte[] buffer = new byte[1024];
        int result;
        while ((result = inputStream.read(buffer))!=-1) {
          outputStream.write(buffer, 0, result);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }finally {
        close(inputStream);
        close(outputStream);
      }
    };

    FileCopyRunner nioBufferCopy = (source, target) -> {
      FileChannel fin = null;
      FileChannel fout = null;
      try {
        fin = new FileInputStream(source).getChannel();
        fout = new FileOutputStream(target).getChannel();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        while (fin.read(byteBuffer)!=-1) {
          byteBuffer.flip();
          while (byteBuffer.hasRemaining()) {
            fout.write(byteBuffer);
          }
          byteBuffer.clear();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        close(fin);
        close(fout);
      }
    };

    FileCopyRunner nioTransferCopy = (source, target) -> {
      FileChannel fin = null;
      FileChannel fout = null;
      try {
        fin = new FileInputStream(source).getChannel();
        fout = new FileInputStream(target).getChannel();
        long transfered = 0L;
        long size = fin.size();
        while (transfered!=size) {
          transfered+=fin.transferTo(0, size, fout);
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        close(fin);
        close(fout);
      }
    };
  }

}
