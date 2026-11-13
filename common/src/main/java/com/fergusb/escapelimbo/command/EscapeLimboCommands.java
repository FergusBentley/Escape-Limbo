package com.fergusb.escapelimbo.command;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.beacon.BeaconRecord;
import com.fergusb.escapelimbo.beacon.BeaconRegistry;
import com.fergusb.escapelimbo.config.EscapeLimboConfig;
import com.fergusb.escapelimbo.mana.PlantMana;
import com.fergusb.escapelimbo.registry.ModItems;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
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
                                .executes(ctx -> giveFragments(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "count")))))
                .then(Commands.literal("plantmana").executes(ctx -> plantMana(ctx.getSource())))
                .then(Commands.literal("beacons").executes(ctx -> listBeacons(ctx.getSource())))
        );
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

    private static int plantMana(CommandSourceStack source) {
        String text = PlantMana.describeNearest(source.getLevel(), BlockPos.containing(source.getPosition()), 16);
        if (text == null) {
            source.sendFailure(Component.literal("No Pitcher Plant or Torchflower within 16 blocks"));
            return 0;
        }
        source.sendSuccess(() -> Component.literal(text), false);
        return 1;
    }

    private static int listBeacons(CommandSourceStack source) {
        BeaconRegistry registry = BeaconRegistry.get(source.getServer());
        long now = BeaconRegistry.now(source.getServer());
        BlockPos origin = BlockPos.containing(source.getPosition());
        int shown = 0;
        for (BeaconRecord record : registry.all()) {
            BlockPos pos = record.pos().pos();
            if (record.pos().dimension() != source.getLevel().dimension() || pos.distManhattan(origin) > 128) continue;
            record.advance(now);
            String line = String.format("%d %d %d: %s, %.1f/%d mana",
                    pos.getX(), pos.getY(), pos.getZ(), record.state().getSerializedName(), record.mana(), record.capacity());
            source.sendSuccess(() -> Component.literal(line), false);
            shown++;
        }
        registry.setDirty();
        if (shown == 0) source.sendSuccess(() -> Component.literal("No Respawn Beacons within 128 blocks"), false);
        return shown;
    }
}