package us.potatoboy.fortress.game.map;

import net.minecraft.text.LiteralText;
import net.minecraft.world.biome.BiomeKeys;
import us.potatoboy.fortress.game.CellManager;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.map.template.TemplateRegion;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.stream.Collectors;

public class FortressMapGenerator {
    public final FortressMapConfig config;

    public FortressMapGenerator(FortressMapConfig config) {
        this.config = config;
    }

    public FortressMap create() throws GameOpenException {
        try {
            MapTemplate template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.id);
            CellManager cellManager = new CellManager(getRegion(template, "cells"));

            FortressMap map = new FortressMap(template, cellManager);
            template.setBiome(BiomeKeys.THE_VOID);

            map.waitingSpawn = getRegion(template, "waiting_spawn");

            map.redSpawns.addAll(template.getMetadata().getRegions("red_spawn").map(TemplateRegion::getBounds).collect(Collectors.toList()));
            map.blueSpawns.addAll(template.getMetadata().getRegions("blue_spawn").map(TemplateRegion::getBounds).collect(Collectors.toList()));

            return map;
        } catch (IOException e) {
            throw new GameOpenException(new LiteralText("Failed to load map"));
        }
    }

    private static BlockBounds getRegion(MapTemplate template, String name) {
        BlockBounds bounds = template.getMetadata().getFirstRegionBounds(name);
        if (bounds == null) {
            throw new GameOpenException(new LiteralText(String.format("%s region not found", name)));
        }

        return bounds;
    }
}
