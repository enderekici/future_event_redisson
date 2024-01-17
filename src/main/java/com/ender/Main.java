package com.ender;

import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import org.redisson.api.RPriorityQueue;
import org.redisson.api.RScoredSortedSet;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

  static Random random = new Random();

  public static void main(String[] args) {

//    RedissonClient redisson = Redisson.create();
//    RPriorityQueue<FutureEvent> queue = redisson.getPriorityQueue("queue1");
//    startProducer(queue);
//    startConsumer(queue);

    /*RScoredSortedSet<FutureEvent> set = redisson.getScoredSortedSet("set2");
    startProducer(set);
    startConsumer(set);*/

    RedisFutureEventExecutor executor = new RedisFutureEventExecutor();

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        executor.scheduleFutureEvent(generateRandomEvent());
      }
    });
    AtomicLong cnt = new AtomicLong();
    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        //executor.popDueEvents("j1").forEach(x -> System.out.println("popped: " + x+ " " + System.currentTimeMillis()));
        int size = executor.popDueEvents("j1").size();
        if (size > 0) {
          cnt.addAndGet(size);
        }
      }
    });

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        System.out.println("processed: " + cnt.get() + " " + System.currentTimeMillis());
        cnt.set(0);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    });

  }

  private static FutureEvent generateRandomEvent() {
    HashMap<String, String> eventParams = new HashMap<>();
    eventParams.put(System.currentTimeMillis() + "", System.currentTimeMillis() + "");
    return new FutureEvent("a_" + UUID.randomUUID().toString(),
      "j1",
      "e_" + random.nextInt(1000), eventParams,
      System.currentTimeMillis() + random.nextInt(30000));
  }

  private static void startConsumer(RScoredSortedSet<FutureEvent> set) {
    AtomicLong cnt = new AtomicLong();

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        set.valueRange(0, true, System.currentTimeMillis(), true).forEach(event -> {
          set.remove(event);
          cnt.getAndIncrement();
          //System.out.println("Consumer polled event " + event);
          if (cnt.get() % 1000 == 0) {
            System.out.println("Consumer processed " + cnt + " events");
          }
        });
      }
    });
  }

  private static void startProducer(RScoredSortedSet<FutureEvent> set) {
    AtomicLong cnt = new AtomicLong();
    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {

        HashMap<String, String> eventParams = new HashMap<>();
        eventParams.put(System.currentTimeMillis() + "", System.currentTimeMillis() + "");
        FutureEvent futureEvent = new FutureEvent("a_" + random.nextInt(10),
          "s_" + random.nextInt(5),
          "e_" + random.nextInt(5), eventParams,
          System.currentTimeMillis() + random.nextInt(30000));

        if (set.contains(futureEvent)) {
          System.out.println("contains " + futureEvent);
          set.remove(futureEvent);
        }
        set.add(futureEvent.getDueDate(), futureEvent);
        System.out.println("Producer added " + futureEvent);

        if (cnt.incrementAndGet() % 1000 == 0) {
          System.out.println("Producer added " + cnt + " events");
        }
      }
    });
  }

  private static void startConsumer(RPriorityQueue<FutureEvent> queue) {
    AtomicLong cnt = new AtomicLong();

    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {
        FutureEvent peek = queue.peek();
        if (peek != null && peek.getDueDate() < System.currentTimeMillis()) {
          FutureEvent event = queue.poll();
          if (event != null) {
            cnt.getAndIncrement();
            System.out.println("Consumer polled event " + event);
            if (cnt.get() % 1000 == 0) {
              System.out.println("Consumer processed " + cnt + " events");
            }
          } else {
            System.out.println("Consumer polled null event");
          }
        }
        Thread.sleep(1000);
      }
    });
  }

  private static void startProducer(RPriorityQueue<FutureEvent> queue) {
    AtomicLong cnt = new AtomicLong();
    Executors.newSingleThreadExecutor().submit(() -> {
      while (true) {

        HashMap<String, String> eventParams = new HashMap<>();
        eventParams.put(System.currentTimeMillis() + "", System.currentTimeMillis() + "");
        FutureEvent futureEvent = new FutureEvent("a_" + random.nextInt(5),
          "s_" + random.nextInt(5),
          "e_" + random.nextInt(5), eventParams,
          System.currentTimeMillis() + random.nextInt(30000));

        if (queue.contains(futureEvent)) {
          System.out.println("contains will update...." + futureEvent);
          queue.remove(futureEvent);
        }
        queue.add(futureEvent);
        System.out.println("Producer added " + futureEvent);

        if (cnt.incrementAndGet() % 1000 == 0) {
          System.out.println("Producer added " + cnt + " events");
        }
        Thread.sleep(1000);
      }
    });

  }
}
