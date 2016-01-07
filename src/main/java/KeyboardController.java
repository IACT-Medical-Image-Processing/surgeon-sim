import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;

/*
    This class basically just wraps Java's Robot interface to allow for key control.
 */
public class KeyboardController implements Observer {
    private Robot robot;
    private HashMap<Integer, Boolean> status;
    private HashMap<Integer, Integer> presses;

    private static KeyboardController kbCtrl;

    public KeyboardController() {
        this.status = new HashMap<Integer, Boolean>();
        this.presses = new HashMap<Integer, Integer>();
        for (Integer i : KeyMap.getInstance().getAllowed()) {
            this.status.put(i, false);
            this.presses.put(i, 0);
        }

        this.robot = ControlRobot.getRobot();

        BPSurgeonSim.getStatusListener().addObserver(this);
    }

    public static void init() {
        kbCtrl = new KeyboardController();
    }

    public static KeyboardController getInstance() {
        return kbCtrl;
    }

    public boolean setMovement(int keyCode, boolean activate) {
        if (!BPSurgeonSim.getStatusListener().getActive()) {
            return false;
        }
        if (KeyMap.getInstance().isKeyAllowed(keyCode)) {
            if (activate == status.get(keyCode)) {
                return false;
            }
            if (activate) {
                System.out.println("Pushing " + keyCode);
                this.status.put(keyCode, true);
                robot.keyPress(keyCode);
            } else {
                System.out.println("Unpushing " + keyCode);
                this.status.put(keyCode, false);
                robot.keyRelease(keyCode);
            }
            return true;
        }
        return false;
    }

    public void printDebug() {
        System.out.println("== CURRENT ==");
        for (Integer i : KeyMap.getInstance().getAllowed()) {
            System.out.printf("Key: %d currently: %s, pressing: %d\n", i, this.status.get(i) ? "pressed" : "released", this.presses.get(i));
        }
        System.out.println("=============");
    }

    public int diffPress (int keyCode, int amount) {
        int newCount = presses.get(keyCode) + amount;
        if (newCount < 0) newCount = 0;
        presses.put(keyCode, newCount);

        return newCount;
    }

    public int getMax() {
        int lastMax = 0;
        for (int i : presses.values()) {
            if (i > lastMax) lastMax = i;
        }

        return lastMax;
    }

    public void reset() {
        for (Integer i : KeyMap.getInstance().getAllowed()) {
            setMovement(i, false);
            this.presses.put(i, 0);
        }
    }

    public void update(Observable o, Object arg) {
        if (o.getClass().equals(StatusListener.class)) {
            StatusListener lis = (StatusListener) o;
            // If the listener will be disabled, disable all keypresses
            if (!lis.getActive()) {
                for (Map.Entry<Integer, Boolean> entry : status.entrySet()) {
                    if (entry.getValue()) {
                        robot.keyRelease(entry.getKey());
                        status.put(entry.getKey(), false);
                    }
                }
            }
        }
    }
}
