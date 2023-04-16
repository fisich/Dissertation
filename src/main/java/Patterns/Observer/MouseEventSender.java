package Patterns.Observer;

import java.util.ArrayList;
import java.util.List;

public abstract class MouseEventSender implements IMouseEventSender {
    protected List<IMouseEventReceiver> observers;

    public MouseEventSender() {
        this.observers = new ArrayList<IMouseEventReceiver>();
    }

    @Override
    public void AddObserver(IMouseEventReceiver observer) {
        observers.add(observer);
    }

    @Override
    public void RemoveObserver(IMouseEventReceiver observer) {
        observers.remove(observer);
    }
}
