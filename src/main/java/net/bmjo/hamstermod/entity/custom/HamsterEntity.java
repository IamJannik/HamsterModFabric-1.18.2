package net.bmjo.hamstermod.entity.custom;

import com.google.common.collect.Lists;
import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.item.ModItems;
import net.bmjo.hamstermod.entity.ModEntities;
import net.bmjo.hamstermod.entity.variant.HamsterVariant;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoWaterTargeting;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HamsterEntity extends TameableEntity implements IAnimatable {
    private AnimationFactory factory = new AnimationFactory(this);
    private int cannotEnterWheelTicks;
    int ticksLeftToFindWheel;
    @Nullable
    BlockPos wheelPos;
    MoveToWheelGoal moveToWheelGoal;
    public HamsterEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    /* GETTER AND SETTER */



    /* METHODS */

    @Nullable
    @Override
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        HamsterEntity baby = ModEntities.HAMSTER.create(world);
        HamsterVariant variant = Util.getRandom(HamsterVariant.values(), this.random);
        baby.setVariant(variant);
        return baby;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.getItem() == ModItems.MEALWORM;
    }

    public static DefaultAttributeContainer.Builder setAttributes() {
        return TameableEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25f);
    }

    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 1.25));
        this.goalSelector.add(2, new HamsterEntity.EnterWheelGoal());
        this.goalSelector.add(3, new HamsterEntity.FindWheelGoal());
        this.moveToWheelGoal = new HamsterEntity.MoveToWheelGoal();
        this.goalSelector.add(4, this.moveToWheelGoal);
        this.goalSelector.add(5, new WanderAroundPointOfInterestGoal(this, 0.75f, false));
        this.goalSelector.add(6, new SitGoal(this));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.75f, 1));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.goalSelector.add(9, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));

        this.targetSelector.add(1, new AnimalMateGoal(this, 1.0));
    }

    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        if (event.isMoving()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster.walk", true));
            return PlayState.CONTINUE;
        }
        if (this.isSitting()) {
            event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster.sitting", true));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.hamster.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData animationData) {
        animationData.addAnimationController(new AnimationController(this, "controller",
                0, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_DOLPHIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_PIG_STEP, 0.15f, 1.0f);
    }

    /* TAMABLE ENTITY*/
    private static final TrackedData<Boolean> SITTING =
            DataTracker.registerData(HamsterEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemstack = player.getStackInHand(hand);
        Item item = itemstack.getItem();

        Item itemForTaming = ModItems.MEALWORM;

        if(isBreedingItem(itemstack) && getBreedingAge() == 0) {
            return super.interactMob(player, hand);
        }

        if (item == itemForTaming && !isTamed()) {
            if (this.world.isClient()) {
                return ActionResult.CONSUME;
            } else {
                if (!player.getAbilities().creativeMode) {
                    itemstack.decrement(1);
                }

                if (!this.world.isClient()) {
                    super.setOwner(player);
                    this.navigation.recalculatePath();
                    this.setTarget(null);
                    this.world.sendEntityStatus(this, (byte)7);
                    setSit(true);
                }

                return ActionResult.SUCCESS;
            }
        }

        if(isTamed() && !this.world.isClient() && hand == Hand.MAIN_HAND) {
            setSit(!isSitting());
            return ActionResult.SUCCESS;
        }

        if (itemstack.getItem() == itemForTaming) {
            return ActionResult.PASS;
        }

        return super.interactMob(player, hand);
    }

    public void setSit(boolean sitting) {
        this.dataTracker.set(SITTING, sitting);
        super.setSitting(sitting);
    }

    public boolean isSitting() {
        return this.dataTracker.get(SITTING);
    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        if (tamed) {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(20.0D);
        } else {
            getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(10.0D);
        }
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putBoolean("isSitting", this.dataTracker.get(SITTING));
        nbt.putInt("Variant", this.getTypeVariant());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(SITTING, nbt.getBoolean("isSitting"));
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, nbt.getInt("Variant"));
    }

    @Override
    public AbstractTeam getScoreboardTeam() {
        return super.getScoreboardTeam();
    }

    public boolean canBeLeashedBy(PlayerEntity player) {
        return false;
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SITTING, false);
        this.dataTracker.startTracking(DATA_ID_TYPE_VARIANT, 0);
    }

    /* VARIANTS */
    private static final TrackedData<Integer> DATA_ID_TYPE_VARIANT =
            DataTracker.registerData(HamsterEntity.class, TrackedDataHandlerRegistry.INTEGER);

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty,
                                 SpawnReason spawnReason, @Nullable EntityData entityData,
                                 @Nullable NbtCompound entityNbt) {
        HamsterVariant variant = Util.getRandom(HamsterVariant.values(), this.random);
        setVariant(variant);
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public HamsterVariant getVariant() {
        return HamsterVariant.byId(this.getTypeVariant() & 255);
    }

    private int getTypeVariant() {
        return this.dataTracker.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariant(HamsterVariant variant) {
        this.dataTracker.set(DATA_ID_TYPE_VARIANT, variant.getId() & 255);
    }

    /* FindWheelStuff */

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.world.isClient) {
            if (this.cannotEnterWheelTicks > 0) {
                --this.cannotEnterWheelTicks;
            }
            if (this.ticksLeftToFindWheel > 0) {
                --this.ticksLeftToFindWheel;
            }
            if (this.age % 20 == 0 && !this.isWheelValid()) {
                this.wheelPos = null;
            }
        }
    }

    void startMovingTo(BlockPos pos) {
        Vec3d vec3d = Vec3d.ofBottomCenter(pos);
        int i = 0;
        BlockPos blockPos = this.getBlockPos();
        int j = (int)vec3d.y - blockPos.getY();
        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int m = blockPos.getManhattanDistance(pos);
        if (m < 15) {
            k = m / 2;
            l = m / 2;
        }

        Vec3d vec3d2 = NoWaterTargeting.find(this, k, l, i, vec3d, 0.3141592741012573);
        if (vec3d2 != null) {
            this.navigation.setRangeMultiplier(0.5F);
            this.navigation.startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, 1.0);
        }
    }

    boolean isWheelValid() {
        if (!this.hasWheel()) {
            return false;
        }
        BlockEntity blockEntity = this.world.getBlockEntity(this.wheelPos);
        return blockEntity != null && blockEntity.getType() == ModBlockEntities.HAMSTER_WHEEL;
    }

    public boolean hasWheel() {
        return this.wheelPos != null;
    }

    boolean canEnterWheel() {
        if (this.cannotEnterWheelTicks > 0 || this.isSitting()) {
            return false;
        }
        return true;
    }

    private boolean doesWheelHaveSpace(BlockPos pos) { //TODO
        BlockEntity blockEntity = this.world.getBlockEntity(pos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)blockEntity).isFullOfBees();
        } else {
            return false;
        }
    }

    boolean isWithinDistance(BlockPos pos, int distance) {
        return pos.isWithinDistance(this.getBlockPos(), (double)distance);
    }

    boolean isTooFar(BlockPos pos) {
        return !this.isWithinDistance(pos, 32);
    }

    class EnterWheelGoal extends Goal {
        EnterWheelGoal() {
            super();
        }

        @Override
        public boolean canStart() {
            if (HamsterEntity.this.hasWheel() && HamsterEntity.this.canEnterWheel() && HamsterEntity.this.wheelPos.isWithinDistance(HamsterEntity.this.getPos(), 2.0)) {
                BlockEntity blockEntity = HamsterEntity.this.world.getBlockEntity(HamsterEntity.this.wheelPos);
                if (blockEntity instanceof BeehiveBlockEntity) {
                    BeehiveBlockEntity beehiveBlockEntity = (BeehiveBlockEntity)blockEntity;
                    if (!beehiveBlockEntity.isFullOfBees()) {
                        return true;
                    }

                    HamsterEntity.this.wheelPos = null;
                }
            }

            return false;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            BlockEntity blockEntity = HamsterEntity.this.world.getBlockEntity(HamsterEntity.this.wheelPos);
            if (blockEntity instanceof BeehiveBlockEntity beehiveBlockEntity) {
                beehiveBlockEntity.tryEnterHive(HamsterEntity.this, false);
            }

        }
    }

    class FindWheelGoal extends Goal {
        FindWheelGoal() {
        }

        @Override
        public boolean canStart() {
            return HamsterEntity.this.ticksLeftToFindWheel == 0 && !HamsterEntity.this.hasWheel() && HamsterEntity.this.canEnterWheel();
        }

        @Override
        public boolean shouldContinue() {
            return canStart();
        }



        @Override
        public void start() {
            HamsterEntity.this.ticksLeftToFindWheel = 200;
            List<BlockPos> list = this.getNearbyFreeWheels();
            if (list.isEmpty()) {
                return;
            }
            for (BlockPos blockPos : list) {
                if (HamsterEntity.this.moveToWheelGoal.isPossibleWheel(blockPos)) continue;
                HamsterEntity.this.wheelPos = blockPos;
                return;
            }
            HamsterEntity.this.moveToWheelGoal.clearPossibleWheels();
            HamsterEntity.this.wheelPos = list.get(0);
        }

        private List<BlockPos> getNearbyFreeWheels() { //TODO to Wheel
            BlockPos blockPos = HamsterEntity.this.getBlockPos();
            PointOfInterestStorage pointOfInterestStorage = ((ServerWorld)HamsterEntity.this.world).getPointOfInterestStorage();
            Stream<PointOfInterest> stream = pointOfInterestStorage.getInCircle((poiType) -> {
                return poiType == PointOfInterestType.BEEHIVE || poiType == PointOfInterestType.BEE_NEST;
            }, blockPos, 20, PointOfInterestStorage.OccupationStatus.ANY);
            return (List)stream.map(PointOfInterest::getPos).filter(HamsterEntity.this::doesWheelHaveSpace).sorted(Comparator.comparingDouble((blockPos2) -> {
                return blockPos2.getSquaredDistance(blockPos);
            })).collect(Collectors.toList());
        }
    }

    @Debug
    public class MoveToWheelGoal extends Goal {
        public static final int field_30295 = 600;
        int ticks;
        private static final int field_30296 = 3;
        final List<BlockPos> possibleWheels;
        @Nullable
        private Path path;
        private static final int field_30297 = 60;
        private int ticksUntilLost;

        MoveToWheelGoal() {
            this.ticks = HamsterEntity.this.world.random.nextInt(10);
            this.possibleWheels = Lists.newArrayList();
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return HamsterEntity.this.wheelPos != null && !HamsterEntity.this.hasPositionTarget() && HamsterEntity.this.canEnterWheel() && !this.isCloseEnough(HamsterEntity.this.wheelPos); // TODO entfernt  && HamsterEntity.this.world.getBlockState(HamsterEntity.this.wheelPos).isIn(BlockTags.BEEWheelS)
        }

        @Override
        public boolean shouldContinue() {
            return this.canStart();
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            HamsterEntity.this.navigation.stop();
            HamsterEntity.this.navigation.resetRangeMultiplier();
        }

        @Override
        public void tick() {
            if (HamsterEntity.this.wheelPos == null) {
                return;
            }
            ++this.ticks;
            if (this.ticks > this.getTickCount(600)) {
                this.makeChosenWheelPossibleWheel();
                return;
            }
            if (HamsterEntity.this.navigation.isFollowingPath()) {
                return;
            }
            if (HamsterEntity.this.isWithinDistance(HamsterEntity.this.wheelPos, 16)) {
                boolean bl = this.startMovingToFar(HamsterEntity.this.wheelPos);
                if (!bl) {
                    this.makeChosenWheelPossibleWheel();
                } else if (this.path != null && HamsterEntity.this.navigation.getCurrentPath().equalsPath(this.path)) {
                    ++this.ticksUntilLost;
                    if (this.ticksUntilLost > 100) { 
                        this.setLost();
                        this.ticksUntilLost = 0;
                    }
                } else {
                    this.path = HamsterEntity.this.navigation.getCurrentPath();
                }
                return;
            }
            if (HamsterEntity.this.isTooFar(HamsterEntity.this.wheelPos)) {
                this.setLost();
                return;
            }
            HamsterEntity.this.startMovingTo(HamsterEntity.this.wheelPos);
        }

        private boolean startMovingToFar(BlockPos pos) {
            HamsterEntity.this.navigation.setRangeMultiplier(10.0f);
            HamsterEntity.this.navigation.startMovingTo(pos.getX(), pos.getY(), pos.getZ(), 1.0);
            return HamsterEntity.this.navigation.getCurrentPath() != null && HamsterEntity.this.navigation.getCurrentPath().reachesTarget();
        }

        boolean isPossibleWheel(BlockPos pos) {
            return this.possibleWheels.contains(pos);
        }

        private void addPossibleWheel(BlockPos pos) {
            this.possibleWheels.add(pos);
            while (this.possibleWheels.size() > 3) {
                this.possibleWheels.remove(0);
            }
        }

        void clearPossibleWheels() {
            this.possibleWheels.clear();
        }

        private void makeChosenWheelPossibleWheel() {
            if (HamsterEntity.this.wheelPos != null) {
                this.addPossibleWheel(HamsterEntity.this.wheelPos);
            }
            this.setLost();
        }

        private void setLost() {
            HamsterEntity.this.wheelPos = null;
            HamsterEntity.this.ticksLeftToFindWheel = 200;
        }

        private boolean isCloseEnough(BlockPos pos) {
            if (HamsterEntity.this.isWithinDistance(pos, 2)) {
                return true;
            }
            Path path = HamsterEntity.this.navigation.getCurrentPath();
            return path != null && path.getTarget().equals(pos) && path.reachesTarget() && path.isFinished();
        }
    }
}
