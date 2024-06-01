package com.nexia.ffa.kits.utilities.player;

public class SavedPlayerData {

    public int kills;
    public int killstreak;
    public int bestKillstreak;
    public int deaths;
    public double rating;
    public double relative_increase;
    public double relative_decrease;
    public SavedPlayerData() {
        this.kills = 0;
        this.killstreak = 0;
        this.bestKillstreak = 0;

        this.deaths = 0;

        this.rating = 0.5;
        this.relative_increase = 0;
        this.relative_decrease = 0;
    }
}
