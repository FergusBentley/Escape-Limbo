package com.fergusb.escapelimbo.beacon;

import com.fergusb.escapelimbo.registry.ModBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The registry holds the data; this keeps the block's look in sync and handles removal. */
public class RespawnBeaconBlockEntity extends BlockEntity {
    private static final int REFRESH_INTERVAL = 20;

    public RespawnBeaconBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESPAWN_BEACON.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RespawnBeaconBlockEntity beacon) {
        if (level.getGameTime() % REFRESH_INTERVAL == 0 && level instanceof ServerLevel serverLevel) refresh(serverLevel, pos);
    }

    /** Brings the record up to date, checks the sky, and updates the block. Creates a record if missing (e.g. /setblock). */
    public static BeaconRecord refresh(ServerLevel level, BlockPos pos) {
        BeaconRegistry registry = BeaconRegistry.get(level.getServer());
        GlobalPos globalPos = GlobalPos.of(level.dimension(), pos);
        BeaconRecord record = registry.getUpdated(level.getServer(), globalPos);
        if (record == null) {
            record = BeaconRecord.placed(globalPos, BeaconRegistry.now(level.getServer()), Optional.empty());
            registry.put(record);
        }
        record.setSkyClear(BeaconDisplay.canSeeSky(level, pos));
        BeaconDisplay.show(level, pos, level.getBlockState(pos), record);
        return record;
    }

    /** Any removal (broken, exploded, replaced by a command): forget the beacon. */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            BeaconRegistry.get(serverLevel.getServer()).remove(GlobalPos.of(level.dimension(), pos));
        }
    }
}