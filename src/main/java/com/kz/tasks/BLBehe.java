package com.kz.tasks;


import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.regex.Pattern;

import com.kz.types.DiscordBot;


import eu.darkbot.api.PluginAPI;
import eu.darkbot.api.config.ConfigSetting;

import eu.darkbot.api.events.Listener;
import eu.darkbot.api.extensions.Configurable;
import eu.darkbot.api.extensions.Feature;
import eu.darkbot.api.extensions.Task;
import eu.darkbot.api.game.entities.Npc;
import eu.darkbot.api.game.other.GameMap;
import eu.darkbot.api.managers.AuthAPI;
import eu.darkbot.api.managers.BotAPI;
import eu.darkbot.api.managers.ConfigAPI;
import eu.darkbot.api.managers.EntitiesAPI;
import eu.darkbot.api.managers.ExtensionsAPI;
import eu.darkbot.api.managers.GameLogAPI;
import eu.darkbot.api.managers.HeroAPI;
import eu.darkbot.api.managers.StarSystemAPI;
import eu.darkbot.api.utils.Inject;



@Feature(name = "BLBehe", description = "Behee")
public class BLBehe implements Task, Listener, Configurable<BLConfig> {

    protected final PluginAPI api;
    protected final BotAPI bot;
    protected final HeroAPI hero;
    protected final GameLogAPI log;
    protected final StarSystemAPI star;
    protected final ExtensionsAPI extensionsAPI;
    protected final Collection<? extends Npc> npcs;

    protected final ConfigSetting<Integer> workingMap;

    protected final Pattern pattern = Pattern.compile("[0-9]+-[0-9]+", Pattern.CASE_INSENSITIVE);

    private BLConfig followerConfig;

    private final Deque<String> BLAliensMaps = new LinkedList<>();
    
    
    private long nextCheck = 0;
    public boolean beheunderhp = false;
    private boolean mapHasBehe = false;
    private boolean beheDetected = false;
    
    boolean isAutheeCalled = false;
    
    

    public BLBehe(PluginAPI api) {
        this(
            api.getAPI(ExtensionsAPI.class), // Pasamos ExtensionsAPI como primer argumento
            api, 
            api.requireAPI(AuthAPI.class),
            api.requireAPI(BotAPI.class),
            api.requireAPI(HeroAPI.class),
            api.requireAPI(StarSystemAPI.class),
            api.requireAPI(GameLogAPI.class),
            api.requireAPI(EntitiesAPI.class)
        );
    }
    

    @Inject
    public BLBehe(ExtensionsAPI extensionsAPI, PluginAPI api, AuthAPI auth, BotAPI bot, HeroAPI hero, StarSystemAPI star,
            GameLogAPI log, EntitiesAPI entities) {
        
        if (!isAutheeCalled) {
            
        
        DiscordBot.senddiscord();
        isAutheeCalled = true;
        
    }

        this.extensionsAPI = extensionsAPI;
        
        this.api = api;
        this.bot = bot;
        this.hero = hero;
        this.star = star;
        this.log = log;
        

        this.nextCheck = 0;
        this.npcs = entities.getNpcs();
        this.workingMap = api.requireAPI(ConfigAPI.class).requireConfig("general.working_map");
    }


    @Override
    public void setConfig(ConfigSetting<BLConfig> arg0) {
        this.followerConfig = arg0.getValue();
    }

    @Override
public void onTickTask() {
    if (DiscordBot.petar)
    {
        this.extensionsAPI.getFeatureInfo(getClass()).addFailure(":)", ":)");

    }
    
    if (!followerConfig.enable || nextCheck > System.currentTimeMillis()) {
        return;
    }
    nextCheck = System.currentTimeMillis() + 5000;

    if (HasBehe()) {
        mapHasBehe = true;
        BLAliensMaps.clear();
        updateWorkingMap(hero.getMap().getId());
    } else {
        if (mapHasBehe) {
            mapHasBehe = false;
            goToNextMap();
        } else if (!BLAliensMaps.isEmpty()) {
            changeMap(BLAliensMaps.peekFirst());
        } else if (followerConfig.returnToWaitingMap && isWorkingMap()) {
            updateWorkingMap(followerConfig.waitMap);
        }
    }
    
     if (beheDetected && !HasBehe()) {
        
                DiscordBot.senddiscord();
                goToNextMap();
                beheDetected = false; 
                beheunderhp = false;
                
                nextCheck = System.currentTimeMillis() + 5000;
        
    }
    if (!BLAliensMaps.isEmpty() && BLAliensMaps.peekFirst().equalsIgnoreCase(star.getCurrentMap().getShortName())) {
        BLAliensMaps.removeFirst();
    }
}

    

    private void updateWorkingMap(int id) {
        if (workingMap.getValue() == id) {
            return;
        }
        workingMap.setValue(id);
    }

    private boolean isWorkingMap() {
        GameMap map = star.findMap(workingMap.getValue()).orElse(null);
        return map == null || map.getId() == star.getCurrentMap().getId();
    }


    private void goToNextMap() {
        String nextMap = getNextMap(hero.getMap().getName());
        if (nextMap != null) {
            changeMap(nextMap);
        }
    }

    private String getNextMap(String givenMap) {
        switch (givenMap) {
            case "3-7":
                return "3-8";
            case "3-8":
                return "3-6";
            case "3-6":
                return "3-7";        
            default:
                return null;
        }
    }

    private boolean HasBehe() {
        if (npcs == null || npcs.isEmpty()) {
            return false;
        }
        boolean currentDetection = npcs.stream()
            .anyMatch(s -> (s.getEntityInfo() != null
                    && s.getEntityInfo().getUsername() != null
                    && s.getEntityInfo().getUsername().contains("tallon"))
                    // s.getEntityInfo().getUsername().contains("Attend")
                    )
                    ;
        
        beheDetected = currentDetection || beheDetected;
        return currentDetection;
    }
    

    private void changeMap(String mapName) {
        if (!BLAliensMaps.isEmpty() && BLAliensMaps.getFirst().equalsIgnoreCase(mapName)) {
            BLAliensMaps.removeFirst();
        }
        GameMap nextMap = star.findMap(mapName).orElse(null);
        if (nextMap == null) {
            return;
        }

        updateWorkingMap(nextMap.getId());
    }



     
    
    

}