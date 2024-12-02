package us.potatoboy.fortress.game.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.world.biome.BiomeKeys;
import us.potatoboy.fortress.game.CellManager;
import xyz.nucleoid.map_templates.BlockBounds;
import xyz.nucleoid.map_templates.MapTemplate;
import xyz.nucleoid.map_templates.MapTemplateSerializer;
import xyz.nucleoid.plasmid.api.game.GameOpenException;

import java.io.IOException;

public record FortressMapGenerator(FortressMapConfig config) {

    public FortressMap create(MinecraftServer server) throws GameOpenException {
        try {
            MapTemplate template = MapTemplateSerializer.loadFromResource(server, this.config.id());
            CellManager cellManager = new CellManager(getRegion(template, "cells"));

            FortressMap map = new FortressMap(template, cellManager);
            template.setBiome(BiomeKeys.THE_VOID);

            map.waitingSpawn = getRegion(template, "waiting_spawn");

            template.getMetadata().getRegionBounds("disabled").forEach(cellManager::disableCells);

            map.redSpawns.addAll(template.getMetadata().getRegionBounds("red_spawn").toList());
            map.blueSpawns.addAll(template.getMetadata().getRegionBounds("blue_spawn").toList());

            return map;
        } catch (IOException e) {
            throw new GameOpenException(Text.literal("Failed to load map"));
        }
    }

    private static BlockBounds getRegion(MapTemplate template, String name) {
        BlockBounds bounds = template.getMetadata().getFirstRegionBounds(name);
        if (bounds == null) {
            throw new GameOpenException(Text.literal(String.format("%s region not found", name)));
        }

        return bounds;
    }
}
