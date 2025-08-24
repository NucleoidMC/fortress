package us.potatoboy.fortress.game.active;

import net.minecraft.text.Text;
import xyz.nucleoid.plasmid.api.game.common.team.GameTeamKey;

public class FortressPlayer {
    public GameTeamKey team;
    public Text displayName;

    public long timeOfDeath;
    public long timeOfSpawn;

    public int kills;
    public int captures;
    public int deaths;

    public FortressPlayer(GameTeamKey team) {
        this.team = team;
    }
}
