package net.bmjo.hamstermod.block.entity.custom;

import com.google.common.collect.Lists;
import net.bmjo.hamstermod.block.custom.HamsterWheel;
import net.bmjo.hamstermod.entity.custom.HamsterEntity;
import net.bmjo.hamstermod.item.ModItems;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.screen.HamsterWheelScreenHandler;
import net.bmjo.hamstermod.util.inventory.ImplementedInventory;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
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

import java.util.List;

public class HamsterWheelBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(4, ItemStack.EMPTY);

    private Hamster hamster;

    public HamsterWheelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAMSTER_WHEEL, pos, state);
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText("itemEntity.hamstermod.hamster_wheel");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new HamsterWheelScreenHandler(syncId, inv, this);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        Inventories.readNbt(nbt, inventory);
        super.readNbt(nbt);
    }

    public static void tick(World world, BlockPos pos, BlockState state, HamsterWheelBlockEntity entity) {
        if (hasRecipe(entity) && hasNotReachedStackLimit(entity)) {
            craftItem(entity);
        }
    }

    private static void craftItem(HamsterWheelBlockEntity entity) {
        entity.removeStack(0, 1);
        entity.removeStack(1, 1);
        entity.removeStack(2, 1);

        entity.setStack(3, new ItemStack(ModItems.FIDGET_SPINNER,
                entity.getStack(3).getCount() + 1));
    }

    private static boolean hasRecipe(HamsterWheelBlockEntity entity) {
        boolean hasItemInFirstSlot = entity.getStack(0).getItem() == Items.BLUE_TERRACOTTA;
        boolean hasItemInSecondSlot = entity.getStack(1).getItem() == ModItems.BALL_BEARING;
        boolean hasItemInThirdSlot = entity.getStack(2).getItem() == Items.COAL_BLOCK;

        return hasItemInFirstSlot && hasItemInSecondSlot && hasItemInThirdSlot;
    }

    private static boolean hasNotReachedStackLimit(HamsterWheelBlockEntity entity) {
        return entity.getStack(3).getCount() < entity.getStack(3).getMaxCount();
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController<HamsterWheelBlockEntity>
                (this, "controller", 0, this::predicate));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster_wheel.swing", true));


        return PlayState.CONTINUE;
    }

    /* HAMSTER STUFF */

    public boolean isEmpty() {
        return this.hamster == null;
    }

    private boolean tryReleaseHamster(BlockState state, BeehiveBlockEntity.BeeState beeState) {
        if (true) { //TODO
            this.hamster = null;
            return true;
        }
        return false;
    }

    public void tryEnterWheel(Entity entity) {
        this.tryEnterHive(entity, 0);
    }

    public void tryEnterHive(Entity entity, int ticksInHive) {
        if (hamster != null) {
            return;
        }
        entity.stopRiding();
        entity.removeAllPassengers();
        NbtCompound nbtCompound = new NbtCompound();
        entity.saveNbt(nbtCompound);
        this.addHamster(nbtCompound, ticksInHive);
        if (this.world != null) {
            BlockPos blockPos = this.getPos();
            this.world.playSound(null, (double)blockPos.getX(), (double)blockPos.getY(), blockPos.getZ(), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        entity.discard();
    }

    public void addHamster(NbtCompound nbtCompound, int ticksInHive) {
        hamster = new Hamster(nbtCompound, ticksInHive);
    }

    private static boolean releaseHamster(World world, BlockPos pos, BlockState state2, Hamster hamster, @Nullable List<Entity> entities, HamsterState hamsterState, @Nullable BlockPos flowerPos) {
        boolean bl;
        if (hamsterState == HamsterWheelBlockEntity.HamsterState.NEW) { //TODO wann drinne bleiben
            return false;
        }
        NbtCompound nbtCompound = hamster.entityData.copy();
        nbtCompound.put("WheelPos", NbtHelper.fromBlockPos(pos));
        nbtCompound.putBoolean("NoGravity", true);
        Direction direction = state2.get(HamsterWheel.FACING);
        BlockPos blockPos = pos.offset(direction);
        boolean bl2 = bl = !world.getBlockState(blockPos).getCollisionShape(world, blockPos).isEmpty();
        if (bl && hamsterState == HamsterWheelBlockEntity.HamsterState.NEW) {
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
            world.playSound(null, pos, SoundEvents.BLOCK_BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return world.spawnEntity(entity2);
        }
        return false;
    }

    public static enum HamsterState {
        NEW,
        MIDDLE,
        OLD;

    }

    static class Hamster {
        final NbtCompound entityData;
        int ticksInHive;

        Hamster(NbtCompound entityData, int ticksInHiv) {
            this.entityData = entityData;
            this.ticksInHive = ticksInHive;
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }


}
