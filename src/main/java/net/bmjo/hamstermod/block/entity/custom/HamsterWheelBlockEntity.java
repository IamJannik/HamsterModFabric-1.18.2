package net.bmjo.hamstermod.block.entity.custom;

import com.google.common.collect.Lists;
import net.bmjo.hamstermod.block.custom.HamsterWheel;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.bmjo.hamstermod.item.ModItems;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.screen.HamsterWheelScreenHandler;
import net.bmjo.hamstermod.util.inventory.ImplementedInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
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

public class HamsterWheelBlockEntity extends BlockEntity implements IAnimatable {
    public static final String ENTITY_DATA_KEY = "EntityData";
    public static final String ENDURANCE_KEY = "Endurance";
    public static final String HAMSTER_KEY = "Hamster";
    private AnimationFactory factory = new AnimationFactory(this);
    private Hamster hamster;

    public HamsterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAMSTER_WHEEL, pos, state);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.put(HAMSTER_KEY, this.getHamsterNBT());
    }

    public NbtList getHamsterNBT() {
        NbtList nbtList = new NbtList();
        if (hamster != null) {
            NbtCompound nbtCompound = hamster.entityData.copy();
            nbtCompound.remove("UUID");
            NbtCompound nbtCompound2 = new NbtCompound();
            nbtCompound2.put(ENTITY_DATA_KEY, nbtCompound);
            nbtCompound2.putInt(ENDURANCE_KEY, hamster.endurance);
            nbtList.add(nbtCompound2);
        }
        return nbtList;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.hamster = null;
        NbtList nbtList = nbt.getList(HAMSTER_KEY, 10);
        for (int i = 0; i < nbtList.size(); ++i) {
            NbtCompound nbtCompound = nbtList.getCompound(i);
            HamsterWheelBlockEntity.Hamster hamster = new HamsterWheelBlockEntity.Hamster(nbtCompound.getCompound(ENTITY_DATA_KEY), nbtCompound.getInt(ENDURANCE_KEY));
            this.hamster = hamster;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, HamsterWheelBlockEntity entity) {
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (true) {
            System.out.println("SPIN");
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster_wheel.spin", true));
            return PlayState.CONTINUE;
        }

        System.out.println("SWING");
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster_wheel.swing", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",0, this::predicate));
    }

    /* HAMSTER STUFF */

    public boolean isFull() {
        return this.hamster != null;
    }

    public List<Entity> tryReleaseHamster(BlockState state, boolean isBroken) {
        ArrayList<Entity> list = Lists.newArrayList();
        releaseHamster(this.world, this.pos, state, hamster, list, isBroken);
        if (!list.isEmpty()) {
            super.markDirty();
        }
        return list;
    }

    public void tryEnterWheel(Entity entity) {
        this.tryEnterWheel(entity, 0);
    }

    public void tryEnterWheel(Entity entity, int endurance) {
        if (hamster != null) {
            return;
        }
        entity.stopRiding();
        entity.removeAllPassengers();
        NbtCompound nbtCompound = new NbtCompound();
        entity.saveNbt(nbtCompound);
        this.addHamster(nbtCompound, endurance);
        if (this.world != null) {
            BlockPos blockPos = this.getPos();
            this.world.playSound(null, (double)blockPos.getX(), (double)blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }

        entity.discard();
    }

    public void addHamster(NbtCompound nbtCompound, int endurance) {
        hamster = new Hamster(nbtCompound, endurance);
    }

    private static boolean releaseHamster(World world, BlockPos pos, BlockState state2, Hamster hamster, @Nullable List<Entity> entities, boolean isBroke) {
        boolean bl;
        if (!isBroke  && hamster.endurance > 1000) {
            return false;
        }
        NbtCompound nbtCompound = hamster.entityData.copy();
        nbtCompound.put("WheelPos", NbtHelper.fromBlockPos(pos));
        nbtCompound.putBoolean("NoGravity", true);
        Direction direction = state2.get(HamsterWheel.FACING);
        BlockPos blockPos = pos.offset(direction);
        bl = !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty();
        if (bl && !isBroke) {
            return false;
        }
        Entity entity2 = EntityType.loadEntityWithPassengers(nbtCompound, world, entity -> entity);
        if (entity2 != null) {
            if (entity2 instanceof HamsterEntity) {
                HamsterEntity hamsterEntity = (HamsterEntity)entity2;
                if (entities != null) {
                    entities.add(hamsterEntity);
                }
                float f = entity2.getWidth();
                double d = bl ? 0.0 : 0.55 + (double)(f / 2.0f);
                double e = (double)pos.getX() + 0.5 + d * (double)direction.getOffsetX();
                double g = (double)pos.getY() + 0.5 - (double)(entity2.getHeight() / 2.0f);
                double h = (double)pos.getZ() + 0.5 + d * (double)direction.getOffsetZ();
                entity2.refreshPositionAndAngles(e, g, h, entity2.getYaw(), entity2.getPitch());
            }
            world.playSound(null, pos, SoundEvents.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.0f);
            System.out.println("SPAWN");
            return world.spawnEntity(entity2);
        }
        return false;
    }

    static class Hamster {
        final NbtCompound entityData;
        int endurance;

        Hamster(NbtCompound entityData, int endurance) {
            this.entityData = entityData;
            this.endurance = endurance;
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


}
