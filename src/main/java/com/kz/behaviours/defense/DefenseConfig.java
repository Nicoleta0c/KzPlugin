package com.kz.behaviours.defense;

import java.util.EnumSet;
import java.util.Set;

import com.kz.modules.pvp.AntiPush;
import com.kz.types.config.ExtraKeyConditions;
import com.kz.types.config.ExtraKeyConditionsSelectable;
import com.github.manolo8.darkbot.config.Config.Loot.Sab;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Option;
import eu.darkbot.api.config.annotations.Percentage;
import eu.darkbot.api.config.annotations.Number;
import com.github.manolo8.darkbot.config.PlayerTag;
import com.github.manolo8.darkbot.config.types.Tag;
import com.github.manolo8.darkbot.config.types.TagDefault;

@Configuration("defense")
public class DefenseConfig {

    @Option("defense.attack")
    public boolean attack = true;

    @Option("defense.activetagger")
    public boolean activetagger = false;

    @Option("defense.tag")
    @Tag(TagDefault.ALL)
    public PlayerTag defense_TAG = null;

    @Option("defense.attackinzone")
    public boolean attackinzone = false;
    
    @Option("defense.respond_attacks")
    public boolean respondAttacks = true;

    @Option("defense.help_list")
    @Dropdown(multi = true)
    public Set<HelpList> helpList = EnumSet.of(HelpList.ALLY, HelpList.CLAN, HelpList.GROUP);

    @Option("defense.go_to_group")
    public boolean goToGroup = true;

    @Option("defense.help_attack")
    public boolean helpAttack = true;

    @Option("defense.max_time_out")
    @Number(min = 0, max = 180, step = 1)
    public int maxSecondsTimeOut = 10;

    @Option("pvp_module.max_range_enemy_attacked")
    @Number(min = 0, max = 4000, step = 100)
    public int rangeForAttackedEnemy = 1500;

    @Option("pvp_module.rangeautoattack")
    @Number(min = 0, max = 8000, step = 100)
    public int rangeautoattack = 900;

    @Option("defense.movement_mode")
    @Dropdown
    public MovementMode movementMode = MovementMode.VSSAFETY;

    @Option("defense.ignore_enemies")
    public boolean ignoreEnemies = true;

    @Option("general.default_ammo")
    public Character ammoKey;

    @Option("general.rsb")
    public boolean useRSB = false;

    @Option("defense.run_config_min_health")
    @Percentage
    public double healthToChange = 0.0;

    @Option("config.loot.sab")
    public Sab SAB = new Sab();

    @Option("general.ability")
    public ExtraKeyConditions ability = new ExtraKeyConditions();

    @Option("general.ish")
    public ExtraKeyConditions ISH = new ExtraKeyConditions();

    @Option("general.smb")
    public ExtraKeyConditions SMB = new ExtraKeyConditions();

    @Option("general.pem")
    public ExtraKeyConditions PEM = new ExtraKeyConditions();

    @Option("general.item_condition")
    public ExtraKeyConditionsSelectable selectable1 = new ExtraKeyConditionsSelectable();

    @Option("general.item_condition")
    public ExtraKeyConditionsSelectable selectable2 = new ExtraKeyConditionsSelectable();

    @Option("general.item_condition")
    public ExtraKeyConditionsSelectable selectable3 = new ExtraKeyConditionsSelectable();

    @Option("general.item_condition")
    public ExtraKeyConditionsSelectable selectable4 = new ExtraKeyConditionsSelectable();

    @Option("general.item_condition")
    public ExtraKeyConditionsSelectable selectable5 = new ExtraKeyConditionsSelectable();

    @Option("anti_push")
    public AntiPush antiPush = new AntiPush();
}
