package com.fergusb.escapelimbo.command;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.config.EscapeLimboConfig;
import com.fergusb.escapelimbo.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class EscapeLimboCommands {
    private EscapeLimboCommands() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("escapelimbo")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("config").executes(ctx -> showConfig(ctx.getSource())))
                .then(Commands.literal("fragments")
                        .executes(ctx -> giveFragments(ctx.getSource(), 1))
                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                .executes(ctx -> giveFragments(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count"))))));
    }

    private static int showConfig(CommandSourceStack source) {
        EscapeLimboConfig config = EscapeLimbo.config();
        source.sendSuccess(() -> Component.literal("Starter beacon: " + config.starterBeacon
                + ", starting mana: " + config.startingMana), false);
        return 1;
    }

    private static int giveFragments(CommandSourceStack source, int count) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        player.addItem(new ItemStack(ModItems.SOUL_FRAGMENT.get(), count));
        source.sendSuccess(() -> Component.literal("Gave " + count + " Soul Fragment" + (count == 1 ? "" : "s")), false);
        return count;
    }
}