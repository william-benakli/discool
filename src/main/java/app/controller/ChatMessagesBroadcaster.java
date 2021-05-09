package app.controller;

import app.model.chat.ChatMessage;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class ChatMessagesBroadcaster {

    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<BiConsumer<String, ChatMessage>> listeners = new LinkedList<>();

    public static synchronized Registration register(BiConsumer<String, ChatMessage> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (ChatMessagesBroadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(String type, ChatMessage message) {
        for (BiConsumer<String, ChatMessage> listener : listeners) {
            executor.execute(() -> listener.accept(type, message));
        }
    }
}

