package Patterns.Observer;

public interface IMouseEventSender {
    void AddObserver(IMouseEventReceiver observer);
    void RemoveObserver(IMouseEventReceiver observer);
    void NotifyObservers(double x, double y);
}
