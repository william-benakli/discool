package app.controller.broadcasters;

import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MessageNotificationBroadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<Consumer<Boolean>> listeners = new LinkedList<>();

    public static synchronized Registration register(Consumer<Boolean> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (MoodleBroadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(Boolean notify) {
        for (Consumer<Boolean> listener : listeners) {
            executor.execute(() -> listener.accept(notify));
        }
    }
}
