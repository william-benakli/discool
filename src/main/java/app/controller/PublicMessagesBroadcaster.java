package app.controller;

import app.web.views.TextChannelView;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class PublicMessagesBroadcaster {

    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<BiConsumer<String, TextChannelView.MessageLayout>> listeners = new LinkedList<>();

    public static synchronized Registration register(BiConsumer<String, TextChannelView.MessageLayout> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (PublicMessagesBroadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(String type, TextChannelView.MessageLayout message) {
        for (BiConsumer<String, TextChannelView.MessageLayout> listener : listeners) {
            executor.execute(() -> listener.accept(type, message));
        }
    }
}