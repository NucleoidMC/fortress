package us.potatoboy.fortress.game.map;

import net.minecraft.text.LiteralText;
import net.minecraft.world.biome.BiomeKeys;
import us.potatoboy.fortress.game.CellManager;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;
import java.util.Optional;
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

            template.getMetadata().getRegionBounds("disabled").forEach(cellManager::disableCells);

            map.redSpawns.addAll(template.getMetadata().getRegionBounds("red_spawn").collect(Collectors.toList()));
            map.blueSpawns.addAll(template.getMetadata().getRegionBounds("blue_spawn").collect(Collectors.toList()));
            if (template.getMetadata().getData().contains("roof")) cellManager.roofHeight = Optional.of(template.getMetadata().getData().getInt("roof"));

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
