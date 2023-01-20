package net.bmjo.hamstermod.block.entity.custom;

import com.google.common.collect.Lists;
import net.bmjo.hamstermod.block.ModBlocks;
import net.bmjo.hamstermod.block.custom.HamsterWheel;
import net.bmjo.hamstermod.entity.ModEntities;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class HamsterWheelBlockEntity extends BlockEntity implements IAnimatable {
    public static final String ENTITY_DATA_KEY = "EntityData";
    public static final String ENDURANCE_KEY = "Endurance";
    public static final String HAMSTER_KEY = "Hamster";
    private final AnimationFactory factory = new AnimationFactory(this);
    private boolean hamster;
    private int endurance;

    public HamsterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAMSTER_WHEEL, pos, state);
        this.hamster = false;
    }

    public static void tick(World world, BlockPos pos, BlockState state, HamsterWheelBlockEntity entity) {
        HamsterWheelBlockEntity.tickHamster(world, pos, state, entity);
    }

    private static void tickHamster(World world, BlockPos pos, BlockState state, HamsterWheelBlockEntity entity) {
            if (entity.hamster) {
                ArrayList<Entity> list = Lists.newArrayList();
                if (entity.endurance < 250 && HamsterWheelBlockEntity.releaseHamster(world, pos, state, entity.hamster, list, false)) {
                    entity.hamster = false;
                    if (world != null) {
                        world.setBlockState(pos, world.getBlockState(pos).with(HamsterWheel.EMPTY, true));
                    }
                    return;
                }
                entity.endurance--;
            }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putBoolean(HAMSTER_KEY, this.hamster);
        nbt.putInt(ENDURANCE_KEY, this.endurance);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        hamster = false;
        if (nbt.contains(HAMSTER_KEY)) {
            this.hamster = nbt.getBoolean(HAMSTER_KEY);
        }
        this.endurance = nbt.getInt(ENDURANCE_KEY);
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() == ModBlocks.HAMSTER_WHEEL && !blockState.get(HamsterWheel.EMPTY)) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster_wheel.spin", true));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster_wheel.swing", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",0, this::predicate));
    }

    /* HAMSTER STUFF */

    public void tryEnterWheel(HamsterEntity hamster, int endurance) {
        if (this.hamster || world.getBlockState(pos).getBlock() != ModBlocks.HAMSTER_WHEEL && endurance > 1500) {
            return;
        }
        hamster.stopRiding();
        hamster.removeAllPassengers();
        NbtCompound nbtCompound = new NbtCompound();
        hamster.saveNbt(nbtCompound);

        Entity newHamster = EntityType.loadEntityWithPassengers(nbtCompound, world, entity -> entity);
        if (newHamster != null) {
            if (newHamster instanceof HamsterEntity hamsterEntity) {
                hamsterEntity.setIsInWheel(true);
                BlockState blockState = world.getBlockState(pos);
                Direction direction = blockState.get(HamsterWheel.FACING);

                double x = (double)pos.getX() + 0.5;
                double y = (double)pos.getY() + 0.15;
                double z = (double)pos.getZ() + 0.5;
                float yaw;
                float pitch = 0f;
                switch (direction) {
                    case EAST -> yaw = 0f;
                    case SOUTH -> yaw =  90f;
                    case WEST -> yaw =  180f;
                    default -> yaw = 270f;
                }
                hamsterEntity.refreshPositionAndAngles(x, y, z, yaw, pitch);
                hamsterEntity.setHeadYaw(yaw);
                if (this.world != null) {
                    BlockPos blockPos = this.getPos();
                    this.world.playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

                    hamster.discard();
                    world.spawnEntity(hamsterEntity);
                }
                this.addHamster(hamsterEntity, endurance);
            }
        }
    }

    public void addHamster(HamsterEntity hamster, int endurance) {
        this.hamster = true;
        this.endurance = endurance;
        world.setBlockState(pos, world.getBlockState(pos).with(HamsterWheel.EMPTY, false));

    }

    public void tryReleaseHamster(BlockState state, boolean urgent) {
        ArrayList<Entity> list = Lists.newArrayList();
        if (releaseHamster(this.world, this.pos, state, hamster, list, urgent)) {
            hamster = false;
            endurance = 0;
            BlockState blockState = world.getBlockState(pos);
            if (world != null && blockState.getBlock() == ModBlocks.HAMSTER_WHEEL) {
                world.setBlockState(pos, blockState.with(HamsterWheel.EMPTY, true));
            }
        }
    }

    private static boolean releaseHamster(World world, BlockPos pos, BlockState state, boolean hamsterBoolean, @Nullable List<Entity> entities, boolean urgent) {
        boolean bl;
        if (!hamsterBoolean || !urgent ) {
            return false;
        }
        Direction direction = state.get(HamsterWheel.FACING);
        BlockPos blockPos = pos.offset(direction);
        bl = !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty();


        if (bl && !urgent) {
            return false;
        }

        Entity entity = world.getClosestEntity(HamsterEntity.class, TargetPredicate.createNonAttackable().setBaseMaxDistance(6.0), null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.2, new Box(pos.getX() + 0.5, pos.getY(), pos.getZ(), pos.getX() +1 , pos.getY() + 1, pos.getZ() + 1));
        if(entity instanceof HamsterEntity hamster) {
            float f = hamster.getWidth();
            double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
            double e = (double)pos.getX() + 0.5 + d * (double)direction.getOffsetX();
            double g = (double)pos.getY() + 0.5 - (double)(hamster.getHeight() / 2.0f);
            double h = (double)pos.getZ() + 0.5 + d * (double)direction.getOffsetZ();
            hamster.refreshPositionAndAngles(e, g, h, hamster.getYaw(), hamster.getPitch());

            hamster.setIsInWheel(false);

            world.playSound(null, pos, SoundEvents.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        return true;
    }

    public boolean isFull() {
        return this.hamster;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


}
