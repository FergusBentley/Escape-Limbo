package com.fergusb.escapelimbo.beacon;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.fergusb.escapelimbo.mana.Mana;
import com.fergusb.escapelimbo.network.BeaconFillPayload;
import com.fergusb.escapelimbo.registry.ModBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;

public class RespawnBeaconBlock extends BaseEntityBlock {
    public RespawnBeaconBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(BeaconDisplay.defaults(stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BeaconDisplay.STATE, BeaconDisplay.FILL, BeaconDisplay.LIGHT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BeaconDisplay.animate(state, level, pos, random, ParticleTypes.GLOW);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RespawnBeaconBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.RESPAWN_BEACON.get(), RespawnBeaconBlockEntity::serverTick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level instanceof ServerLevel serverLevel) {
            BeaconRegistry registry = BeaconRegistry.get(serverLevel.getServer());
            registry.put(BeaconRecord.placed(GlobalPos.of(level.dimension(), pos), BeaconRegistry.now(serverLevel.getServer()),
                    Optional.ofNullable(placer).map(LivingEntity::getUUID)));
            RespawnBeaconBlockEntity.refresh(serverLevel, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel serverLevel)) return InteractionResult.SUCCESS;

        BeaconRecord record = RespawnBeaconBlockEntity.refresh(serverLevel, pos);
        int perUse = Math.max(1, EscapeLimbo.config().beaconFillPerSecond / 5);
        int wanted = (int) Math.min(perUse, Math.floor(record.capacity() - record.mana()));
        if (wanted <= 0) return InteractionResult.PASS;
        int taken = Mana.take(player, wanted);
        if (taken <= 0) return InteractionResult.PASS;

        record.addMana(taken);
        BeaconRegistry.get(serverLevel.getServer()).setDirty();
        BeaconDisplay.show(level, pos, state, record);
        // XP pickup sound that climbs toward full, plus the on-screen bar.
        float fullness = (float) (record.mana() / record.capacity());
        level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.12F, 0.5F + 0.7F * fullness);
        if (player instanceof ServerPlayer serverPlayer) {
            EscapeLimbo.platform().sendToPlayer(serverPlayer, new BeaconFillPayload((int) record.mana(), record.capacity()));
        }
        return InteractionResult.SUCCESS;
    }
}