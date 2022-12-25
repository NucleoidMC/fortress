package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import us.potatoboy.fortress.Fortress;

public class FortressItemTags {
    public static final TagKey<Item> MODULES = TagKey.of(RegistryKeys.ITEM, Fortress.identifier("modules"));

    public static final TagKey<Item> REGULAR_MODULES = TagKey.of(RegistryKeys.ITEM, Fortress.identifier("regular_modules"));
    public static final TagKey<Item> SPECIAL_MODULES = TagKey.of(RegistryKeys.ITEM, Fortress.identifier("special_modules"));
}
