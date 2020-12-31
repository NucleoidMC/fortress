package us.potatoboy.fortress.custom.item;

import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import xyz.nucleoid.plasmid.fake.FakeItem;

public class ModuleItem extends Item implements FakeItem {
    private final Item proxy;
    public final Identifier structure;

    public ModuleItem(Item proxy, Identifier structure) {
        super(new Item.Settings());
        this.proxy = proxy;
        this.structure = structure;
    }

    @Override
    public Item asProxy() {
        return proxy;
    }
}
