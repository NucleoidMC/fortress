package us.potatoboy.fortress.custom.item;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import us.potatoboy.fortress.Fortress;

public class FortressItemTags {
    public static final Tag<Item> MODULES = TagFactory.ITEM.create(Fortress.identifier("modules"));

    public static final Tag<Item> REGULAR_MODULES = TagFactory.ITEM.create(Fortress.identifier("regular_modules"));
    public static final Tag<Item> SPECIAL_MODULES = TagFactory.ITEM.create(Fortress.identifier("special_modules"));
}
