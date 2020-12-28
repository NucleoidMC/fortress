package us.potatoboy.fortress.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import us.potatoboy.fortress.game.map.FortressMapConfig;
import xyz.nucleoid.plasmid.game.config.PlayerConfig;

public class FortressConfig {
    public static final Codec<FortressConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FortressMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            Codec.INT.fieldOf("time_limit_mins").forGetter(config -> config.timeLimitMins)
    ).apply(instance, FortressConfig::new));

    public final FortressMapConfig mapConfig;
    public final PlayerConfig playerConfig;
    public final int timeLimitMins;

    public FortressConfig(FortressMapConfig mapConfig, PlayerConfig players, int timeLimitMins) {
        this.mapConfig = mapConfig;
        this.playerConfig = players;
        this.timeLimitMins = timeLimitMins;
    }
}
