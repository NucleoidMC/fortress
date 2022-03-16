package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;
import us.potatoboy.fortress.Fortress;

public class FortressItemTags {
    public static final TagKey<Item> MODULES = TagKey.of(Registry.ITEM_KEY, Fortress.identifier("modules"));

    public static final TagKey<Item> REGULAR_MODULES = TagKey.of(Registry.ITEM_KEY, Fortress.identifier("regular_modules"));
    public static final TagKey<Item> SPECIAL_MODULES = TagKey.of(Registry.ITEM_KEY, Fortress.identifier("special_modules"));
}
