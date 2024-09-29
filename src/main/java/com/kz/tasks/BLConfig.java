package com.kz.tasks;

import com.github.manolo8.darkbot.core.manager.StarManager.MapOptions;
import com.github.manolo8.darkbot.config.Config.PercentRange;

import eu.darkbot.api.config.annotations.Configuration;
import eu.darkbot.api.config.annotations.Dropdown;
import eu.darkbot.api.config.annotations.Option;

@Configuration("BL_follower")
public class BLConfig {
    @Option("general.enabled")
    public boolean enable = false;

    @Option("BL_follower.return_waiting_map")
    public boolean returnToWaitingMap = true;

    @Option("BL_follower.waiting_map")
    @Dropdown(options = MapOptions.class)
    public int waitMap = 8;

    @Option("BL.healnpc")
    public PercentRange healthnpcrange = new PercentRange(0.2, 0.4);

}
