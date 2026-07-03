package us.potatoboy.fortress.game;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public enum CaptureState {
    CAPTURING(Component.literal("Capturing..").withStyle(ChatFormatting.GOLD)),
    SECURING(Component.literal("Securing..").withStyle(ChatFormatting.AQUA)),
    CONTESTED(Component.literal("Contested!"));

    private final Component name;

    CaptureState(Component name) {
        this.name = name;
    }

    public Component getName() {
        return name;
    }
}
