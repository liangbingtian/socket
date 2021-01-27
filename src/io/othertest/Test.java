package io.othertest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 类测试
 *
 * @author liangbingtian
 * @date 2021/01/20 下午2:09
 */
public class Test {

  public static void main(String[] args) {

    AtomicInteger atomicInteger = new AtomicInteger();

    ExecutorService executorService = Executors.newFixedThreadPool(10);
    for (int i = 0; i<10; ++i) {
      executorService.execute(() -> {
        for (int j = 0; j<10;++j) {
          atomicInteger.addAndGet(10);
        }
        System.out.println("当前线程为:"+Thread.currentThread().getName()+"当前值为:"+atomicInteger.get());
      });
    }
  }

}
