package github.scarsz.examinator.util;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.hooks.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * https://github.com/Scarsz/JDA-Utilities/blob/984496d9eac64c231b6966991d7c15d3f4c685e7/src/main/java/com/jagrosh/jdautilities/waiter/EventWaiter.java
 * @author John Grosh (jagrosh)
 */
public class EventWaiter implements EventListener
{
    private final HashMap<Class<?>, List<WaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;

    /**
     * Constructs an empty EventWaiter.
     */
    public EventWaiter()
    {
        waitingEvents = new HashMap<>();
        threadpool = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Waits an indefinite amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>When this occurs, the provided {@link java.util.function.Consumer Consumer} will accept and
     * execute using the same Event.
     *
     * @param  <T>
     *         The type of Event to wait for
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action)
    {
        waitForEvent(classType, condition, action, -1, null, null);
    }

    /**
     * Waits a predetermined amount of time for an {@link net.dv8tion.jda.core.events.Event Event} that
     * returns {@code true} when tested with the provided {@link java.util.function.Predicate Predicate}.
     *
     * <p>Once started, there are two possible outcomes:
     * <ul>
     *     <li>The correct Event occurs within the time alloted, and the provided
     *     {@link java.util.function.Consumer Consumer} will accept and execute using the same Event.</li>
     *
     *     <li>The time limit is elapsed and the provided {@link java.lang.Runnable} is executed.</li>
     * </ul>
     *
     * @param  <T>
     *         The type of Event to wait for
     * @param  classType
     *         The {@link java.lang.Class} of the Event to wait for
     * @param  condition
     *         The Predicate to test when Events of the provided type are thrown
     * @param  action
     *         The Consumer to perform an action when the condition Predicate returns {@code true}
     * @param  timeout
     *         The maximum amount of time to wait for
     * @param  unit
     *         The {@link java.util.concurrent.TimeUnit TimeUnit} measurement of the timeout
     * @param  timeoutAction
     *         The Runnable to run if the time runs out before a correct Event is thrown
     */
    public <T extends Event> void waitForEvent(Class<T> classType, Predicate<T> condition, Consumer<T> action, long timeout, TimeUnit unit, Runnable timeoutAction)
    {
        List<WaitingEvent> list;
        if(waitingEvents.containsKey(classType))
            list = waitingEvents.get(classType);
        else
        {
            list = new ArrayList<>();
            waitingEvents.put(classType, list);
        }
        WaitingEvent we = new WaitingEvent<>(condition, action);
        list.add(we);
        if(timeout>0 && unit!=null)
            threadpool.schedule(() -> {
                if(list.remove(we) && timeoutAction!=null)
                    timeoutAction.run();
            }, timeout, unit);
    }

    @Override
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public final void onEvent(Event event)
    {
        Class c = event.getClass();
        while(c.getSuperclass()!=null) {
            if(waitingEvents.containsKey(c))
            {
                List<WaitingEvent> list = waitingEvents.get(c);
                List<WaitingEvent> ulist = new ArrayList<>(list);
                list.removeAll(ulist.stream().filter(i -> i.attempt(event)).collect(Collectors.toList()));
            }
            if(event instanceof ShutdownEvent)
            {
                threadpool.shutdown();
            }
            c = c.getSuperclass();
        }
    }

    private class WaitingEvent<T extends Event>
    {
        final Predicate<T> condition;
        final Consumer<T> action;

        WaitingEvent(Predicate<T> condition, Consumer<T> action)
        {
            this.condition = condition;
            this.action = action;
        }

        boolean attempt(T event)
        {
            if(condition.test(event))
            {
                action.accept(event);
                return true;
            }
            return false;
        }
    }

}
