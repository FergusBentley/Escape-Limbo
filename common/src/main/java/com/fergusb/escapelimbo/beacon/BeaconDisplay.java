package com.fergusb.escapelimbo.beacon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.levelgen.Heightmap;

public final class BeaconDisplay {
    public static final EnumProperty<RespawnBeaconState> STATE = EnumProperty.create("state", RespawnBeaconState.class);
    public static final IntegerProperty FILL = IntegerProperty.create("fill", 0, BeaconRecord.FILL_LEVELS);
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    private BeaconDisplay() {}

    public static BlockState defaults(BlockState state) {
        return state.setValue(STATE, RespawnBeaconState.UNPRIMED).setValue(FILL, 0).setValue(LIGHT, 0);
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(LIGHT);
    }

    public static void animate(BlockState state, Level level, BlockPos pos, RandomSource random, SimpleParticleType particle) {
        if (state.getValue(STATE) != RespawnBeaconState.ACTIVE || random.nextInt(3) != 0) return;
        level.addParticle(particle,
                pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 0.3 + random.nextDouble() * 0.5, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                0.0, 0.02, 0.0);
    }

    /** Saves the state and plays a sound when it turns on or off */
    public static void show(Level level, BlockPos pos, BlockState current, BeaconRecord record) {
        BlockState updated = current
                .setValue(STATE, record.state())
                .setValue(FILL, record.fillLevel())
                .setValue(LIGHT, record.lightLevel());
        if (updated == current) return;
        level.setBlock(pos, updated, Block.UPDATE_CLIENTS);

        boolean wasActive = current.getValue(STATE) == RespawnBeaconState.ACTIVE;
        boolean isActive = record.state() == RespawnBeaconState.ACTIVE;
        if (isActive != wasActive) {
            level.playSound(null, pos, isActive ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    public static boolean canSeeSky(Level level, BlockPos pos) {
        if (level.dimensionType().hasCeiling()) return false;
        int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
        BlockPos.MutableBlockPos cursor = pos.mutable();
        for (int y = pos.getY() + 1; y < top; y++) {
            BlockState state = level.getBlockState(cursor.setY(y));
            if (!state.getFluidState().isEmpty()) return false;
            if (state.getLightDampening() >= 15 && !state.is(Blocks.BEDROCK)) return false;
        }
        return true;
    }
}