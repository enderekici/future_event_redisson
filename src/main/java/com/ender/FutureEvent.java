package com.ender;

import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FutureEvent implements
  Comparable<FutureEvent> {

  private String key;
  private Map<String, String> eventParams;
  private long dueDate;

  public FutureEvent(String actorId,String journeyName, String eventName, Map<String, String> eventParams, long dueDate) {
    this.key = actorId + "~" + journeyName + "~" + eventName;
    this.eventParams = eventParams;
    this.dueDate = dueDate;
  }

  @Override
  public int compareTo(FutureEvent o) {
    return Long.compare(this.dueDate, o.dueDate);
  }

  public String getJourneyName() {
    return key.split("~")[1];
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FutureEvent that = (FutureEvent) o;
    return Objects.equals(key, that.key);
  }

  public int hashCode() {
    return Objects.hash(key);
  }
}
