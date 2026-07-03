package us.potatoboy.fortress.custom.item;

import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import us.potatoboy.fortress.Fortress;

public class FortressItemTags {
    public static final TagKey<Item> MODULES = TagKey.create(Registries.ITEM, Fortress.identifier("modules"));

    public static final TagKey<Item> REGULAR_MODULES = TagKey.create(Registries.ITEM, Fortress.identifier("regular_modules"));
    public static final TagKey<Item> SPECIAL_MODULES = TagKey.create(Registries.ITEM, Fortress.identifier("special_modules"));
}
