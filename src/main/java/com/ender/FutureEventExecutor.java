package com.ender;

import java.util.Collection;

public interface FutureEventExecutor {

  public void deployJourney(String journeyName);

  public void unDeployJourney(String journeyName);

  public void scheduleFutureEvent(FutureEvent futureEvent);

  Collection<FutureEvent> popDueEvents(String journeyName);
}
