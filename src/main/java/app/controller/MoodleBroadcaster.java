package app.controller;

import app.web.views.MoodleView;
import com.vaadin.flow.shared.Registration;

import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MoodleBroadcaster {
    static Executor executor = Executors.newSingleThreadExecutor();

    static LinkedList<Consumer<MoodleView.SectionLayout>> listeners = new LinkedList<>();

    public static synchronized Registration register(Consumer<MoodleView.SectionLayout> listener) {
        listeners.add(listener);
        return () -> {
            synchronized (MoodleBroadcaster.class) {
                listeners.remove(listener);
            }
        };
    }

    public static synchronized void broadcast(MoodleView.SectionLayout section) {
        for (Consumer<MoodleView.SectionLayout> listener : listeners) {
            executor.execute(() -> listener.accept(section));
        }
    }
}