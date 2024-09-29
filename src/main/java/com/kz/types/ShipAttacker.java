package com.kz.types;

import com.kz.behaviours.defense.DefenseConfig;
import com.kz.types.config.ExtraKeyConditions;
import com.kz.types.config.ExtraKeyConditionsSelectable;
import com.kz.types.suppliers.DefenseLaserSupplier;
import com.github.manolo8.darkbot.Main;
import com.github.manolo8.darkbot.config.Config.Loot.Sab;

import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.types.PercentRange;
import eu.darkbot.api.config.types.ShipMode;
import eu.darkbot.api.config.types.ShipMode.ShipModeImpl;
import eu.darkbot.api.game.entities.Pet;
import eu.darkbot.api.game.entities.Player;
import eu.darkbot.api.game.entities.Portal;
import eu.darkbot.api.game.entities.Ship;
import eu.darkbot.api.game.enums.EntityEffect;
import eu.darkbot.api.game.group.GroupMember;
import eu.darkbot.api.game.items.ItemFlag;
import eu.darkbot.api.game.items.SelectableItem;
import eu.darkbot.api.game.items.SelectableItem.Formation;
import eu.darkbot.api.game.items.SelectableItem.Laser;
import eu.darkbot.api.game.other.Location;
import eu.darkbot.api.game.other.Lockable;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.GroupAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.HeroItemsAPI;
import eu.darkbot.api.managers.MovementAPI;

import java.util.ArrayList;
//import java.util.List;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
//import java.util.Optional;


import static com.github.manolo8.darkbot.Main.API;

public class ShipAttacker {
    protected final PluginAPI api;
    protected final HeroAPI heroapi;
    protected final HeroItemsAPI items;
    protected final MovementAPI movement;
    protected final ConfigAPI configAPI;
    protected final GroupAPI group;
    protected final BotAPI bot;
    protected final ConfigSetting<PercentRange> repairHpRange;
    protected final ConfigSetting<Character> ammoKey;
    protected final Collection<? extends Player> allShips;
    protected final Collection<? extends Portal> allPortals;

    private DefenseLaserSupplier laserSupplier;
    private ConditionsManagement conditionsManagement;
    private Ship target;

    protected long laserTime;
    protected long fixTimes;
    protected long clickDelay;
    protected long keyDelay;
    protected boolean sab;
    private DefenseConfig defense = null;
    private Random rnd;

    protected boolean firstAttack;
    protected long isAttacking;
    protected int fixedTimes;
    protected Character lastShot;

    public ShipAttacker(Main main) {
        this(main.pluginAPI.getAPI(PluginAPI.class), main.config.LOOT.SAB, main.config.LOOT.RSB.ENABLED);
    }

    public ShipAttacker(PluginAPI api, Sab sab, boolean rsbEnabled) {
        this.api = api;
        this.heroapi = api.getAPI(HeroAPI.class);
        this.movement = api.getAPI(MovementAPI.class);
        this.configAPI = api.getAPI(ConfigAPI.class);
        this.group = api.getAPI(GroupAPI.class);
        this.bot = api.getAPI(BotAPI.class);
        EntitiesAPI entities = api.getAPI(EntitiesAPI.class);
        this.allShips = entities.getPlayers();
        this.allPortals = entities.getPortals();
        this.rnd = new Random();
        this.items = api.getAPI(HeroItemsAPI.class);
        this.laserSupplier = new DefenseLaserSupplier(api, heroapi, items, sab, rsbEnabled);

        this.repairHpRange = configAPI.requireConfig("general.safety.repair_hp_range");
        this.ammoKey = configAPI.requireConfig("loot.ammo_key");

        this.conditionsManagement = new ConditionsManagement(api, items);
    }

    public ShipAttacker(PluginAPI api, DefenseConfig defense) {
        this(api, defense.SAB, defense.useRSB);
        this.defense = defense;
    }

    public String getStatus() {
        return getTarget() != null ? "Killing " + getTarget().getEntityInfo().getUsername() : "Idle";
    }

    public Ship getTarget() {
        if (target != null && target.isValid()) {
            return target;
        } else {
            return null;
        }
    }

    public void tryLockTarget() {
        if (heroapi.getTarget() == target && firstAttack) {
            clickDelay = System.currentTimeMillis();
        }

        fixedTimes = 0;
        laserTime = 0;
        firstAttack = false;
        if (heroapi.getLocationInfo().distanceTo(target) < 700) {
            if (System.currentTimeMillis() - clickDelay > 500) {
                heroapi.setLocalTarget(target);
                target.trySelect(false);
                clickDelay = System.currentTimeMillis();
            }
        } else {
            movement.moveTo(target);
        }
    }

    public void setTarget(Ship target) {
        this.target = target;
    }

    public void tryLockAndAttack() {
        if (target == null) {
            return;
        }

        if (heroapi.isAttacking(target)) {
            tryAttackOrFix();
        } else {
            tryLockTarget();
        }
    }

    public void tryAttackOrFix() {

        //llamar pem01
        checkAndUsePem01();

        //funcion 10
        checkHealthAndUseConfig2();

        // Validar si es un enemigo
        if (target == null || !target.isValid() || !target.getEntityInfo().isEnemy()) {
            return; 
        }

        // hacer el ataque solo si es enemigo(tiene dos validaciones) funcion 5 y 8
        if (heroapi.isAttacking(target) || target.isAttacking()) {
            // si esta siendo atacado, sigue al objetivo
            movement.moveTo(target);
            checkAndAttackDisruptor();
        } else {
            tryLockTarget(); 
        }


        if (System.currentTimeMillis() < laserTime) {
            return;
        }

        if (!firstAttack) {
            firstAttack = true;
            sendAttack(1500, 5000, true);
        } else if (lastShot == null || !lastShot.equals(getAttackKey())) {
            sendAttack(250, 5000, true);
        } else if (!heroapi.isAttacking(target) || !heroapi.isAiming(target)) {
            sendAttack(1500, 5000, false);
        } else if (target.getHealth().hpDecreasedIn(1500) || target.hasEffect(EntityEffect.NPC_ISH)
                || target.hasEffect(EntityEffect.ISH)
                || heroapi.getLocationInfo().distanceTo(target) > 700) {
            isAttacking = Math.max(isAttacking, System.currentTimeMillis() + 2000);
        } else if (System.currentTimeMillis() > isAttacking) {
            sendAttack(1500, ++fixedTimes * 3000L, false);
        }else if (countNearbyEnemies() >= 4 && countNearbyEnemies() <= 24) { // Usar Megamana si hay entre 4 y 24 enemigos cercanos
            useMegamana();
        }
    }

    private void sendAttack(long minWait, long bugTime, boolean normal) {
        laserTime = System.currentTimeMillis() + minWait;
        isAttacking = Math.max(isAttacking, laserTime + bugTime);
        if (normal) {
            lastShot = getAttackKey();
            API.keyboardClick(lastShot);
        } else if (target != null && target.isValid()) {
            target.trySelect(true);
        }
    }

    protected Laser getBestLaserAmmo() {
        return laserSupplier.get();
    }

    private Character getAttackKey() {
        return getAttackKey(ammoKey.getValue());
    }

    @SuppressWarnings("deprecation")
    private Character getAttackKey(Character defaultAmmo) {
        Laser laser = getBestLaserAmmo();
        if (laser != null) {
            Character key = items.getKeyBind(laser);
            if (key != null) {
                return key;
            }
        }

        if (defense != null) {
            return defense.ammoKey;
        }

        return defaultAmmo;
    }

    public void vsMove() {
        if (target != null && target.isValid()) {
            double distance = heroapi.getLocationInfo().distanceTo(target);
            Location targetLoc = target.getLocationInfo().destinationInTime(400);
            if (distance > 600) {
                if (movement.canMove(targetLoc)) {
                    movement.moveTo(targetLoc);
                    if (target.getSpeed() > heroapi.getSpeed()) {
                        heroapi.setRunMode();
                    }
                } else {
                    resetDefenseData();
                }
            } else {
                movement.moveTo(Location.of(targetLoc, rnd.nextInt(360), distance));
            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean useKeyWithConditions(ExtraKeyConditions extra, SelectableItem selectableItem) {
        if (extra.enable) {
            if (selectableItem == null && extra.key != null) {
                selectableItem = items.getItem(extra.key);
            }

            if (selectableItem != null && heroapi.getHealth().hpPercent() < extra.healthRange.max
                    && heroapi.getHealth().hpPercent() > extra.healthRange.min
                    && heroapi.getLocalTarget() != null
                    && heroapi.getLocalTarget().getHealth().hpPercent() < extra.healthEnemyRange.max
                    && heroapi.getLocalTarget().getHealth().hpPercent() > extra.healthEnemyRange.min) {

                if (System.currentTimeMillis() - keyDelay < 500) {
                    return false;
                }

                if (conditionsManagement.useSelectableReadyWhenReady(selectableItem)) {
                    keyDelay = System.currentTimeMillis();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean useSelectableReadyWhenReady(SelectableItem selectableItem) {
        if (conditionsManagement.useSelectableReadyWhenReady(selectableItem)) {
            keyDelay = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean useKeyWithConditions(ExtraKeyConditionsSelectable extra) {
        return conditionsManagement.useKeyWithConditions(extra);
    }

    public boolean inGroupAttacked(int id) {
        if (group.hasGroup()) {
            for (GroupMember member : group.getMembers()) {
                if (!member.isDead() && member.getId() == id && member.isAttacked()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean inGroup(int id) {
        if (group.hasGroup()) {
            for (GroupMember member : group.getMembers()) {
                if (member.getId() == id) {
                    return true;
                }
            }
        }
        return false;
    }

    public void goToMemberAttacked() {
        GroupMember member = SharedFunctions.getMemberGroupAttacked(group, heroapi, configAPI);
        if (member != null) {
            movement.moveTo(member.getLocation());
        }
    }

    public GroupMember getClosestMember() {
        if (group.hasGroup()) {
            return group.getMembers().stream()
                    .filter(member -> !member.isDead() && member.getMapId() == heroapi.getMap().getId())
                    .min(Comparator.<GroupMember>comparingDouble(m -> m.getLocation().distanceTo(heroapi)))
                    .orElse(null);
        }
        return null;
    }

    public void setMode(ShipMode config, Formation formation) {
        if (formation != null && !heroapi.isInFormation(formation)) {
            if (items.getItem(formation, ItemFlag.USABLE, ItemFlag.READY).isPresent()) {
                ShipModeImpl mode = new ShipModeImpl(config.getConfiguration(), formation);
                heroapi.setMode(mode);
            } else {
                heroapi.setMode(config);
            }
        } else {
            heroapi.setMode(config);
        }
    }

    public Ship getEnemy(int maxDistance) {
        return getEnemy(maxDistance, new ArrayList<>());
    }

    public Ship getEnemy(int maxDistance, ArrayList<Integer> playersToIgnore) {
        boolean ableToAttack = heroapi.getMap().isPvp()
                || allPortals.stream().noneMatch(p -> heroapi.distanceTo(p) < 1500);
        return allShips.stream()
                .filter(Ship::isValid)
                .filter(s -> s.getId() != heroapi.getId()
                        && s.getEntityInfo().isEnemy()
                        && (ableToAttack || s.isAttacking())
                        && !playersToIgnore.contains(s.getId())
                        && !s.hasEffect(290)
                        && !(s instanceof Pet)
                        && !inGroup(s.getId())
                        && movement.canMove(s)
                        && s.getLocationInfo().distanceTo(heroapi) <= maxDistance)

                .sorted(Comparator.comparingDouble(s -> s.getLocationInfo().distanceTo(heroapi))).findAny()
                .orElse(null);
    }

    public void resetDefenseData() {
        target = null;
        fixedTimes = 0;
        firstAttack = true;
    }

    //Si un jugador enemigo me hace target, usar un pem-01. funcion 6
    @SuppressWarnings("deprecation")
    public void checkAndUsePem01() {
    Lockable target = heroapi.getLocalTarget();
    
    if (target instanceof Ship) {
        Ship enemyTarget = (Ship) target;
        if (enemyTarget.isValid() && enemyTarget.isAttacking()) {
            SelectableItem pem01 = items.getItem('p');
            
            if (pem01 != null && conditionsManagement.useSelectableReadyWhenReady(pem01)) {
                API.keyboardClick(items.getKeyBind(pem01));
            }
        }
    }
}

//atacar a los enemigos. funcion 5
public void checkAndAttackDisruptor() {
    Lockable target = heroapi.getLocalTarget();

    if (target instanceof Ship) {
        Ship enemyTarget = (Ship) target;
        if (enemyTarget.isValid() && enemyTarget.isAttacking() && isDisruptor(enemyTarget)) {
            if (enemyTarget.getEntityInfo().isEnemy()) {
                sendAttack(1500, 5000, true); 
            } else {
                System.out.println("No atacarlo porque no es un enemigo");
            }
        }
    }
}


private boolean isDisruptor(Ship ship) {
    return ship.getEntityInfo().getUsername().contains("Disruptor");
}

//Usar megamina si hay muy cerca de mi +4 jugadores enemigos. funcion 2

private int countNearbyEnemies() {
    return (int) allShips.stream()
            .filter(s -> s.getEntityInfo().isEnemy())
            .filter(s -> heroapi.getLocationInfo().distanceTo(s) <= 700)
            .count();
}

@SuppressWarnings("deprecation")
private void useMegamana() {
    SelectableItem megamana = items.getItem('m');
    if (megamana != null && conditionsManagement.useSelectableReadyWhenReady(megamana)) {
        API.keyboardClick(items.getKeyBind(megamana));
    }
}


//funcion 10
// porcentaje de vida menor
private final double HEALTH_THRESHOLD = 30.0; 

public void checkHealthAndUseConfig2() {
    double currentHealthPercent = heroapi.getHealth().hpPercent();
    // si la vida es menor de 30% usar la configuracion 2
    if (currentHealthPercent < HEALTH_THRESHOLD) {
        ShipMode config2 = (ShipMode) configAPI.requireConfig("configuracion_2"); 
        setMode(config2, null);
    }
}


}