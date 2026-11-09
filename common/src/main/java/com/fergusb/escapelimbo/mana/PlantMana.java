package com.fergusb.escapelimbo.mana;

import com.fergusb.escapelimbo.EscapeLimbo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Mana from Pitcher Plant and Torchflower */
public final class PlantMana extends SavedData {
    public static final int MAX = 10;
    private static final int NIGHT_START = 13000, DAWN = 23000, DAY = 24000;
    private static final int UPDATE_INTERVAL = 20;
    private static final double TORCHFLOWER_RANGE = 5;
    private static final DustParticleOptions BURST = new DustParticleOptions(0x6FFFEB, 0.6F);

    private record Entry(String dimension, long pos, float mana) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("dimension").forGetter(Entry::dimension),
                Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
                Codec.FLOAT.fieldOf("mana").forGetter(Entry::mana)
        ).apply(instance, Entry::new));
    }

    private static final Codec<PlantMana> CODEC = Entry.CODEC.listOf().xmap(PlantMana::new, PlantMana::entries);
    public static final SavedDataType<PlantMana> TYPE = new SavedDataType<>(
            EscapeLimbo.id("plant_mana"), PlantMana::new, CODEC, DataFixTypes.SAVED_DATA_COMMAND_STORAGE);

    private final Map<String, Map<Long, Float>> plants = new HashMap<>();

    private PlantMana() {}

    private PlantMana(List<Entry> entries) {
        for (Entry entry : entries) plants.computeIfAbsent(entry.dimension(), d -> new HashMap<>()).put(entry.pos(), entry.mana());
    }

    private List<Entry> entries() {
        List<Entry> list = new ArrayList<>();
        plants.forEach((dimension, map) -> map.forEach((pos, mana) -> list.add(new Entry(dimension, pos, mana))));
        return list;
    }

    public static PlantMana get(MinecraftServer server) {
        return server.getDataStorage().computeIfAbsent(TYPE);
    }

    private Map<Long, Float> in(ServerLevel level) {
        return plants.computeIfAbsent(level.dimension().identifier().toString(), d -> new HashMap<>());
    }

    private static boolean isPitcher(BlockState state) {
        return state.is(Blocks.PITCHER_PLANT)
                || state.is(Blocks.PITCHER_CROP) && state.getValue(PitcherCropBlock.AGE) == PitcherCropBlock.MAX_AGE;
    }

    private static boolean isTracked(BlockState state) {
        return isPitcher(state) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER || state.is(Blocks.TORCHFLOWER);
    }

    private static BlockPos plantPos(BlockState state, BlockPos pos) {
        if (state.is(Blocks.TORCHFLOWER)) return pos;
        if (!isPitcher(state)) return null;
        return state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    // Pitcher Plants
    public static void onBlockChange(ServerLevel level, BlockPos pos, BlockState oldState, BlockState newState) {
        if (oldState.getBlock() == newState.getBlock() && isTracked(oldState) == isTracked(newState)) return;
        if (isTracked(oldState)) get(level.getServer()).forget(level, pos);
        if (isPitcher(newState) && newState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) get(level.getServer()).track(level, pos);
    }

    public static void onChunkLoad(ServerLevel level, LevelChunk chunk) {
        LevelChunkSection[] sections = chunk.getSections();
        for (int i = 0; i < sections.length; i++) {
            LevelChunkSection section = sections[i];
            if (section.hasOnlyAir() || !section.maybeHas(PlantMana::isPitcher)) continue;
            int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (isPitcher(state) && state.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.LOWER) {
                            get(level.getServer()).track(level, new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z));
                        }
                    }
                }
            }
        }
    }

    private void track(ServerLevel level, BlockPos pos) {
        if (in(level).putIfAbsent(pos.asLong(), 0F) == null) setDirty();
    }

    private void forget(ServerLevel level, BlockPos pos) {
        if (in(level).remove(pos.asLong()) != null) setDirty();
    }

    // Torchflowers
    public static void onDeath(LivingEntity entity) {
        if (!(entity instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) return;
        MinecraftServer server = level.getServer();
        KillPenalty penalty = KillPenalty.get(server);
        String dimension = level.dimension().identifier().toString();
        ChunkPos chunk = ChunkPos.containing(mob.blockPosition());
        float gain = (float) (1 - penalty.penalty(server, dimension, chunk));
        if (gain > 0) {
            PlantMana plants = get(server);
            int r = (int) Math.ceil(TORCHFLOWER_RANGE);
            Vec3 at = mob.position();
            for (BlockPos pos : BlockPos.betweenClosed(mob.blockPosition().offset(-r, -r, -r), mob.blockPosition().offset(r, r, r))) {
                if (!level.getBlockState(pos).is(Blocks.TORCHFLOWER) || Vec3.atCenterOf(pos).distanceTo(at) > TORCHFLOWER_RANGE) continue;
                plants.add(level, pos.immutable(), gain);
            }
        }
        if (mob.getLastHurtByPlayer() == null) penalty.recordKill(server, dimension, chunk);
    }

    private void add(ServerLevel level, BlockPos pos, float amount) {
        in(level).merge(pos.asLong(), amount, (a, b) -> Math.min(MAX, a + b));
        setDirty();
    }

    // Harvesting
    public static InteractionResult onUse(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty() || player.isSpectator()) return InteractionResult.PASS;
        BlockPos plant = plantPos(level.getBlockState(hit.getBlockPos()), hit.getBlockPos());
        if (plant == null) return InteractionResult.PASS;
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        PlantMana plants = get(server.getServer());
        Float mana = plants.in(server).get(plant.asLong());
        int whole = mana == null ? 0 : (int) Math.floor(mana);
        if (whole < 1) return InteractionResult.PASS;
        plants.in(server).put(plant.asLong(), mana - whole);
        plants.setDirty();
        Vec3 tip = tip(server, plant);
        ExperienceOrb.award(server, tip, whole);
        server.sendParticles(BURST, tip.x, tip.y, tip.z, 8, 0.2, 0.2, 0.2, 0);
        server.playSound(null, plant, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.4F);
        return InteractionResult.SUCCESS;
    }

    // Position where orbs appear
    private static Vec3 tip(Level level, BlockPos plant) {
        BlockState state = level.getBlockState(plant);
        Vec3 offset = state.getOffset(plant);
        double height = state.is(Blocks.PITCHER_CROP) ? 1.9875 : isPitcher(state) ? 1.675 : 0.875;
        return new Vec3(plant.getX() + 0.5, plant.getY() + height, plant.getZ() + 0.5).add(offset.x, 0, offset.z);
    }

    public static void tick(MinecraftServer server) {
        if (server.getTickCount() % UPDATE_INTERVAL != 0) return;
        PlantMana plants = get(server);
        for (ServerLevel level : server.getAllLevels()) {
            Map<Long, Float> map = plants.plants.get(level.dimension().identifier().toString());
            if (map == null || map.isEmpty()) continue;
            float target = pitcherRate(level);
            boolean drain = target < 0;
            Iterator<Map.Entry<Long, Float>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Long, Float> entry = it.next();
                BlockPos pos = BlockPos.of(entry.getKey());
                if (!level.isLoaded(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (!isTracked(state)) {
                    it.remove();
                    plants.setDirty();
                    continue;
                }
                float mana = entry.getValue();
                if (isPitcher(state)) {
                    float next = mana;
                    if (drain) next = Math.max(0, mana + target * UPDATE_INTERVAL);
                    else if (target > 0 && level.canSeeSky(pos.above(2))) next = Math.min(MAX, mana + target * UPDATE_INTERVAL);
                    else if (target == 0) next = 0; // daytime
                    if (next != mana) {
                        entry.setValue(next);
                        mana = next;
                        plants.setDirty();
                    }
                }
                if (state.is(Blocks.TORCHFLOWER) && mana <= 0) {
                    it.remove();
                    plants.setDirty();
                    continue;
                }
                if (mana >= 1) hint(level, pos, mana);
            }
        }
    }

    // Pitcher Plant mana per tick
    private static float pitcherRate(ServerLevel level) {
        long time = Math.floorMod(level.getDefaultClockTime(), DAY);
        float peak = MAX * moonFullness(level);
        if (time >= NIGHT_START && time < DAWN) return peak / (DAWN - NIGHT_START);
        if (time >= DAWN) return -(float) MAX / (DAY - DAWN);
        return 0;
    }

    // 1 = full moon, 0 = new moon
    private static float moonFullness(ServerLevel level) {
        int index = level.environmentAttributes().getDimensionValue(EnvironmentAttributes.MOON_PHASE).index();
        return 1 - Math.min(index, 8 - index) / 4F;
    }

    // Effects to show if a plant contains manna
    private static void hint(ServerLevel level, BlockPos pos, float mana) {
        if (level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 48, false) == null) return;
        Vec3 tip = tip(level, pos);
        int count = 1 + (int) (mana / 4); // 1 to 3
        level.sendParticles(ParticleTypes.END_ROD, tip.x, tip.y, tip.z, count, 0.05, 0.05, 0.05, 0.01);
    }

    // Testing
    public static String describeNearest(ServerLevel level, BlockPos around, int range) {
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(around.offset(-range, -range, -range), around.offset(range, range, range))) {
            BlockPos plant = plantPos(level.getBlockState(pos), pos);
            if (plant == null) continue;
            double distance = plant.distSqr(around);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = plant.immutable();
            }
        }
        if (best == null) return null;
        Float mana = get(level.getServer()).in(level).get(best.asLong());
        String kind = isPitcher(level.getBlockState(best)) ? "Pitcher Plant" : "Torchflower";
        String extra = isPitcher(level.getBlockState(best))
                ? String.format(" (moon fullness %.2f, sky %s, clock %d)", moonFullness(level), level.canSeeSky(best.above(2)) ? "seen" : "hidden",
                Math.floorMod(level.getDefaultClockTime(), DAY))
                : "";
        return String.format("%s at %d, %d, %d: %.2f mana%s%s", kind, best.getX(), best.getY(), best.getZ(), mana == null ? 0 : mana,
                mana == null && isPitcher(level.getBlockState(best)) ? " (not tracked yet)" : "", extra);
    }
}