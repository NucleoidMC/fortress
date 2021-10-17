package us.potatoboy.fortress.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import us.potatoboy.fortress.game.map.FortressMapConfig;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;

public record FortressConfig(
        FortressMapConfig mapConfig, PlayerConfig playerConfig, int timeLimitMins, boolean captureEnemy, boolean recapture,
        boolean midJoin, int captureTickDelay
) {
    public static final Codec<FortressConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FortressMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            Codec.INT.fieldOf("time_limit_mins").forGetter(config -> config.timeLimitMins),
            Codec.BOOL.fieldOf("capture_enemy").forGetter(config -> config.captureEnemy),
            Codec.BOOL.optionalFieldOf("recapture", true).forGetter(config -> config.recapture),
            Codec.BOOL.optionalFieldOf("mid_join", true).forGetter(config -> config.midJoin),
            Codec.INT.optionalFieldOf("capture_tick_delay", 10).forGetter(config -> config.captureTickDelay)
    ).apply(instance, FortressConfig::new));
}
