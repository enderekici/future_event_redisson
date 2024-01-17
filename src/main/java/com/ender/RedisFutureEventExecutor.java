package com.ender;

import java.util.ArrayList;
import java.util.Collection;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RedissonClient;


public class RedisFutureEventExecutor implements FutureEventExecutor {

  private RedissonClient redisson;

  public RedisFutureEventExecutor() {
    redisson = Redisson.create();
  }

  private String getStoreKeyName(String journeyName) {
    return "future_k_" + journeyName;
  }
  private String getStoreValueName(String journeyName) {
    return "future_v_" + journeyName;
  }

  @Override
  public void deployJourney(String journeyName) {

  }

  @Override
  public void unDeployJourney(String journeyName) {
    redisson.getScoredSortedSet(getStoreKeyName(journeyName)).delete();
  }

  @Override
  public void scheduleFutureEvent(FutureEvent event) {
    RScoredSortedSet<String> keySet = redisson.getScoredSortedSet(getStoreKeyName(event.getJourneyName()));
    keySet.add(event.getDueDate(), event.getKey());
    redisson.getMap(getStoreValueName(event.getJourneyName())).put(event.getKey(), event);

    //keySet.forEach(System.out::println);

  }

  @Override
  public Collection<FutureEvent> popDueEvents(String journeyName) {
    ArrayList<FutureEvent> events = new ArrayList<>();
    RScoredSortedSet<String> set = redisson.getScoredSortedSet(
      getStoreKeyName(journeyName));
    RMap<String, FutureEvent> map = redisson.getMap(getStoreValueName(journeyName));
    set.valueRange(0, true, System.currentTimeMillis(), true).forEach(key -> {
      set.remove(key);
      events.add(map.remove(key));
    });
    return events;
  }
}
