

import com.epicbot.api.shared.GameType;
import com.epicbot.api.shared.entity.*;
import com.epicbot.api.shared.methods.*;
import com.epicbot.api.shared.model.Tile;
import com.epicbot.api.shared.script.LoopScript;
import com.epicbot.api.shared.script.ScriptManifest;
import com.epicbot.api.shared.util.Random;
import com.epicbot.api.shared.util.time.Time;
import com.epicbot.api.shared.webwalking.model.RSBank;

import java.awt.event.KeyEvent;
import java.util.function.BooleanSupplier;

import static java.lang.Thread.sleep;

@ScriptManifest(name = "EggCollector", gameType = GameType.OS)
public class EggCollector extends LoopScript {

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

    public IEquipmentAPI equipment() {return getAPIContext().equipment();}



    private final Tile SAFE_TILE = new Tile(3200, 9888, 0);
    private final Tile EGGS_POS = new Tile(3178, 9880, 0);

    public boolean shouldHopWorld = false;
    public boolean isInCombat = false;

    public boolean waitHope = false;
    private final String foodType = "Trout";

    public IBankAPI banco() {
        return getAPIContext().bank();
    }

    private ITabsAPI tabs() {
        return getAPIContext().tabs();
    }

    public IMouseAPI mouse() {
        return getAPIContext().mouse();
    }
    public IWidgetsAPI widgets(){return getAPIContext().widgets();}

    private String[] food = {"Trout"};

    private String targetItem;


    private int[] worlds = {379, 380, 382, 383, 384, 394, 397, 398, 399, 418, 417, 430, 431, 433, 434, 435, 436, 437, 451, 452, 453,
            454, 455, 456, 469, 470, 471, 475, 476, 483, 497, 498, 499, 500, 501, 537, 542, 543, 544, 545, 546, 547, 552, 553, 554,
            555, 556, 557, 562, 563, 571, 575};

    @Override
    protected int loop() {

       switchEquip();

        if (!waitHope) {
            eatFood();
            if (inventory().isFull()) {
                bank();
                withdrawTrout();
            }
            Time.sleep(100);

            if (!myInventoryIsFull() && !EGGS_POS.contains(player().get().getCentralPoint())) {
                walkToAndWait(EGGS_POS.getLocation());
            }

            Time.sleep(100);
            if (!myInventoryIsFull() && !EGGS_POS.contains(player().get().getCentralPoint())) {
                GroundItem spiderEGG = getAPIContext().groundItems().query().named("Red spiders' eggs").reachable().results().nearest();
                if (spiderEGG != null && spiderEGG.isVisible()) {
                    if (spiderEGG.interact("Take")) {
                        waitFor(() -> !getAPIContext().localPlayer().isMoving(), 1000);
                        shouldHopWorld = true;
                    }
                    Time.sleep(100);
                } else {
                    webWalking().walkTo(SAFE_TILE.getLocation());
                    waitHope = true;
                    Time.sleep(10000);

                }
                Time.sleep(100);
            }

        } else {
            hopWorld();
            Time.sleep(2000);
            waitHope = false;
        }
        Time.sleep(100);

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
                getAPIContext().webWalking().walkTo(RSBank.VARROCK_EAST.getTile());
            }

        }
    }

    public boolean myInventoryIsFull() {
        return inventory().isFull();
    }


    public void hopWorld() {
        if (!myInventoryIsFull() && shouldHopWorld) {
            int randomWorld = worlds[Random.nextInt(0, worlds.length - 1)];
            getAPIContext().world().hop(randomWorld);
            Time.sleep(1000);
            shouldHopWorld = false;
        }

    }

    public void combat() {
        if (player().isInCombat()) {
            isInCombat = true;
        }
    }

    private void withdrawTrout() {
        banco().depositInventory();
        banco().withdraw(5, foodType);
        Time.sleep(2000);
        banco().close();
    }

    public static int getShortSleepTime() {
        int sleepMin = 445;
        int sleepMax = 887;
        return Random.nextInt(sleepMin, sleepMax);
    }

    public void eatFood() {
        //Check Inventory For Food
        String food = "Trout";

        int currentHealth = player().getHealthPercent();
        if (currentHealth < 75) {
            //Swap to Inventory Tab if it's not open
            if (!tabs().isOpen(ITabsAPI.Tabs.INVENTORY)) {
                tabs().open(ITabsAPI.Tabs.INVENTORY);
            }

            int foodX = inventory().getItem(food).getX();
            int foodY = inventory().getItem(food).getY();
            mouse().click(foodX, foodY);
        }
    }
    public void switchEquip() {
        if (!tabs().isOpen(ITabsAPI.Tabs.EQUIPMENT)) {
            tabs().open((ITabsAPI.Tabs.EQUIPMENT));
        }
        if (tabs().isOpen(ITabsAPI.Tabs.EQUIPMENT)) {
            boolean rightClick = true;
            WidgetChild bankAllButton = widgets().get(387).getChild(20);
            mouse().click(bankAllButton, rightClick);
        }
    }


}

