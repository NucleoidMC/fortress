package us.potatoboy.fortress.game;

import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum CaptureState {
    CAPTURING(new LiteralText("Capturing..").formatted(Formatting.GOLD)),
    SECURING(new LiteralText("Securing..").formatted(Formatting.AQUA)),
    CONTESTED(new LiteralText("Contested!"));

    private final Text name;

    CaptureState(Text name) {
        this.name = name;
    }

    public Text getName() {
        return name;
    }
}
