package net.cavoj.sleepalone;

import net.minecraft.network.MessageType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

public class SleepAloneMod {
    public static void sendAsleepMessage(ServerPlayerEntity player) {
        MutableText message = new LiteralText("");
        message.append(player.getDisplayName());
        MutableText text = new LiteralText(" is sleeping");
        text.setStyle(Style.EMPTY.withColor(Formatting.GREEN));
        message.append(text);
        player.getServer().getPlayerManager().broadcastChatMessage(message, MessageType.CHAT, player.getUuid());

    }
}
