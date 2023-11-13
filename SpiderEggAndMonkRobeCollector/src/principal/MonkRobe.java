package principal;

import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.*;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.util.function.BooleanSupplier;

@ScriptManifest(name = "CollectMonkRobe", gameType = GameType.OS)
public class MonkRobe extends LoopScript {

    @Override
    public boolean onStart(String... strings) {
        return true;
    }

    public IInventoryAPI inventory() {
        return getAPIContext().inventory();
    }

    public ILocalPlayerAPI player() {
        return getAPIContext().localPlayer();
    }

    public IWebWalkingAPI webWalking() {
        return getAPIContext().webWalking();
    }

    public IObjectsAPI localObject() {
        return getAPIContext().objects();
    }

    private final Tile ROBE_POS = new Tile(3058, 3487, 1);
    private final Tile LADDER = new Tile(3057, 3484, 0);

    public boolean shouldHopWorld = false;
    public boolean ladderUp = false;

    private int[] worlds = {379, 380, 382, 383, 384, 394, 397, 398, 399, 418, 417, 430, 431, 433, 434, 435, 436, 437, 451, 452, 453, 454, 455, 456, 469, 470, 471, 475, 476, 483, 497, 498, 499, 500, 501, 537, 542, 543, 544, 545, 546, 547, 552, 553, 554, 555, 556, 557, 562, 563, 571, 575};

    @Override
    protected int loop() {
        if (inventory().isFull()) {
            bank();
            ladderUp = false;
        }

        if (!ladderUp) {
            getLadderUP();
            ladderUp = true;
        }
        Time.sleep(100);
        if (!myInventoryIsFull() && !ROBE_POS.contains(player().get().getCentralPoint()) && ladderUp) {
            walkToAndWait(ROBE_POS.getLocation());
        }
        Time.sleep(100);

        if (!myInventoryIsFull()) {
            GroundItem robeTop = getAPIContext().groundItems().query().named("Monk's robe top").reachable().results().nearest();
            GroundItem robeBottom = getAPIContext().groundItems().query().named("Monk's robe").reachable().results().nearest();
            if (robeBottom != null && robeBottom.isVisible() || robeTop != null && robeTop.isVisible()) {
                robeTop.interact("Take");
                Time.sleep(1500);
                robeBottom.interact("Take");
                ladderUp = true;
                shouldHopWorld = true;
                Time.sleep(20000);
            } else {
                shouldHopWorld = true;
                hopWorld();
            }

        }
        if ((ROBE_POS.contains(player().get().getCentralPoint()) && ladderUp)) {
            shouldHopWorld = true;
            hopWorld();
        }
        return 1;
    }

    private void walkToAndWait(Tile destination) {
        webWalking().walkTo(destination);
        waitFor(() -> !getAPIContext().localPlayer().isMoving(), 5000);
    }

    private void waitFor(BooleanSupplier condition, long timeout) {
        long startTime = System.currentTimeMillis();
        while (!condition.getAsBoolean() && System.currentTimeMillis() - startTime < timeout) {
            Time.sleep(100);
        }

    }


    private void bank() {
        if (getAPIContext().bank().isOpen()) {
            if (getAPIContext().bank().depositInventory()) {
                Time.sleep(2000, () -> getAPIContext().inventory().isEmpty());
            }
        } else {
            if (getAPIContext().bank().isVisible()) {
                if (getAPIContext().bank().open()) {
                    Time.sleep(5000, () -> getAPIContext().bank().isOpen());
                }
            } else {
                getAPIContext().webWalking().walkTo(RSBank.EDGEVILLE.getTile());
            }

        }
    }

    public void getLadderUP() {
        if (!inventory().isFull()) {
            walkToAndWait(LADDER.getLocation());
            localObject().query().nameMatches("Ladder").actions("Climb-up").results().nearest();
        }

    }

    public boolean myInventoryIsFull() {
        return inventory().isFull();
    }

    public boolean hopWorld() {
        if (!myInventoryIsFull() && shouldHopWorld) {
            int randomWorld = worlds[Random.nextInt(0, worlds.length - 1)];
            getAPIContext().world().hop(randomWorld);
            Time.sleep(1000);
            shouldHopWorld = false;
        }

        return false;
    }


}

