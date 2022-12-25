package us.potatoboy.fortress.game;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum CaptureState {
    CAPTURING(Text.literal("Capturing..").formatted(Formatting.GOLD)),
    SECURING(Text.literal("Securing..").formatted(Formatting.AQUA)),
    CONTESTED(Text.literal("Contested!"));

    private final Text name;

    CaptureState(Text name) {
        this.name = name;
    }

    public Text getName() {
        return name;
    }
}
