package net.bmjo.hamstermod.block.custom;

import net.bmjo.hamstermod.block.entity.ModBlockEntities;
import net.bmjo.hamstermod.block.entity.custom.HamsterWheelBlockEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class HamsterWheel extends BlockWithEntity implements BlockEntityProvider{
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty USE = BooleanProperty.of("use");

    public HamsterWheel(Settings settings) {
        super(settings);
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.createCuboidShape(2, 1, 4, 14, 14, 12),
            Block.createCuboidShape(2, 0, 3, 14, 1, 13),
            Block.createCuboidShape(7, 1, 12, 9, 10, 13),
            Block.createCuboidShape(1, 6, 4, 2, 12, 12),
            Block.createCuboidShape(14, 6, 4, 15, 12, 12),
            Block.createCuboidShape(3, 14, 4, 13, 15, 12),
            Block.createCuboidShape(5, 15, 4, 11, 16, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_E = Stream.of(
            Block.createCuboidShape(4, 1, 2, 12, 14, 14),
            Block.createCuboidShape(3, 0, 2, 13, 1, 14),
            Block.createCuboidShape(3, 1, 7, 4, 10, 9),
            Block.createCuboidShape(4, 6, 1, 12, 12, 2),
            Block.createCuboidShape(4, 6, 14, 12, 12, 15),
            Block.createCuboidShape(4, 14, 3, 12, 15, 13),
            Block.createCuboidShape(4, 15, 5, 12, 16, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.createCuboidShape(2, 1, 4, 14, 14, 12),
            Block.createCuboidShape(2, 0, 3, 14, 1, 13),
            Block.createCuboidShape(7, 1, 3, 9, 10, 4),
            Block.createCuboidShape(14, 6, 4, 15, 12, 12),
            Block.createCuboidShape(1, 6, 4, 2, 12, 12),
            Block.createCuboidShape(3, 14, 4, 13, 15, 12),
            Block.createCuboidShape(5, 15, 4, 11, 16, 12)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    private static final VoxelShape SHAPE_W = Stream.of(
            Block.createCuboidShape(4, 1, 2, 12, 14, 14),
            Block.createCuboidShape(3, 0, 2, 13, 1, 14),
            Block.createCuboidShape(12, 1, 7, 13, 10, 9),
            Block.createCuboidShape(4, 6, 14, 12, 12, 15),
            Block.createCuboidShape(4, 6, 1, 12, 12, 2),
            Block.createCuboidShape(4, 14, 3, 12, 15, 13),
            Block.createCuboidShape(4, 15, 5, 12, 16, 11)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;

        };
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable BlockView world, List<Text> tooltip, TooltipContext options) {
        if (Screen.hasShiftDown()) {
            tooltip.add(new TranslatableText("item.hamstermod.hamster_wheel.tooltip.shift"));
        } else {
            tooltip.add(new TranslatableText("item.hamstermod.hamster_wheel.tooltip"));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, USE);
    }


    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(USE) ? 15 : 0;
    }

    @Override
    public void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity) {
        if (!world.isClient) {
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = ((LivingEntity) entity);
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 100));
            }
        }
        super.onSteppedOn(world, pos, state, entity);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    /* BLOCK ENTITY*/

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
            if (hand == Hand.MAIN_HAND) {
                boolean currentState = state.get(USE);
                world.setBlockState(pos, state.with(USE, !currentState), Block.NOTIFY_ALL);
            }
            if (screenHandlerFactory != null) {
                player.openHandledScreen(screenHandlerFactory);
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!world.isClient && blockEntity instanceof HamsterWheelBlockEntity) {
                HamsterWheelBlockEntity hamsterWheelBlockEntity = (HamsterWheelBlockEntity)blockEntity;
                hamsterWheelBlockEntity.tryReleaseHamster(state, true);
                world.updateNeighbors(pos, state.getBlock());
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HamsterWheelBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlockEntities.HAMSTER_WHEEL, HamsterWheelBlockEntity::tick);
    }
}
