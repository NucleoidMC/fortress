package us.potatoboy.fortress.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public class FortressMapConfig {
    public static final Codec<FortressMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id),
            Codec.INT.optionalFieldOf("build_limit", 15).forGetter(config -> config.buildLimit)
    ).apply(instance, FortressMapConfig::new));

    public final Identifier id;
    public final Integer buildLimit;

    public FortressMapConfig(Identifier id, Integer buildLimit) {
        this.id = id;
        this.buildLimit = buildLimit;
    }
}
