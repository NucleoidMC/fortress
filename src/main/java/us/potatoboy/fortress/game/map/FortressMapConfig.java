package us.potatoboy.fortress.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;

public class FortressMapConfig {
    public static final Codec<FortressMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id)
    ).apply(instance, FortressMapConfig::new));

    public Identifier id;

    public FortressMapConfig(Identifier id) {
        this.id = id;
    }
}
