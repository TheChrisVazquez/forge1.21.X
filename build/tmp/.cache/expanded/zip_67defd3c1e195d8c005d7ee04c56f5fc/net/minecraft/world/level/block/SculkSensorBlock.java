package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.SculkSensorPhase;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkSensorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<SculkSensorBlock> CODEC = simpleCodec(SculkSensorBlock::new);
    public static final int ACTIVE_TICKS = 30;
    public static final int COOLDOWN_TICKS = 10;
    public static final EnumProperty<SculkSensorPhase> PHASE = BlockStateProperties.SCULK_SENSOR_PHASE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final float[] RESONANCE_PITCH_BEND = Util.make(new float[16], p_277301_ -> {
        int[] aint = new int[]{0, 0, 2, 4, 6, 7, 9, 10, 12, 14, 15, 18, 19, 21, 22, 24};

        for (int i = 0; i < 16; i++) {
            p_277301_[i] = NoteBlock.getPitchFromNote(aint[i]);
        }
    });

    @Override
    public MapCodec<? extends SculkSensorBlock> codec() {
        return CODEC;
    }

    public SculkSensorBlock(BlockBehaviour.Properties p_277588_) {
        super(p_277588_);
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(PHASE, SculkSensorPhase.INACTIVE)
                .setValue(POWER, Integer.valueOf(0))
                .setValue(WATERLOGGED, Boolean.valueOf(false))
        );
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        BlockPos blockpos = pContext.getClickedPos();
        FluidState fluidstate = pContext.getLevel().getFluidState(blockpos);
        return this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    @Override
    protected FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        if (getPhase(pState) != SculkSensorPhase.ACTIVE) {
            if (getPhase(pState) == SculkSensorPhase.COOLDOWN) {
                pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.INACTIVE), 3);
                if (!pState.getValue(WATERLOGGED)) {
                    pLevel.playSound(null, pPos, SoundEvents.SCULK_CLICKING_STOP, SoundSource.BLOCKS, 1.0F, pLevel.random.nextFloat() * 0.2F + 0.8F);
                }
            }
        } else {
            deactivate(pLevel, pPos, pState);
        }
    }

    @Override
    public void stepOn(Level pLevel, BlockPos pPos, BlockState pState, Entity pEntity) {
        if (!pLevel.isClientSide()
            && canActivate(pState)
            && pEntity.getType() != EntityType.WARDEN
            && pLevel.getBlockEntity(pPos) instanceof SculkSensorBlockEntity sculksensorblockentity
            && pLevel instanceof ServerLevel serverlevel
            && sculksensorblockentity.getVibrationUser().canReceiveVibration(serverlevel, pPos, GameEvent.STEP, GameEvent.Context.of(pState))) {
            sculksensorblockentity.getListener().forceScheduleVibration(serverlevel, GameEvent.STEP, GameEvent.Context.of(pEntity), pEntity.position());
        }

        super.stepOn(pLevel, pPos, pState, pEntity);
    }

    @Override
    protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
        if (!pLevel.isClientSide() && !pState.is(pOldState.getBlock())) {
            if (pState.getValue(POWER) > 0 && !pLevel.getBlockTicks().hasScheduledTick(pPos, this)) {
                pLevel.setBlock(pPos, pState.setValue(POWER, Integer.valueOf(0)), 18);
            }
        }
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pMovedByPiston) {
        if (!pState.is(pNewState.getBlock())) {
            super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
            if (getPhase(pState) == SculkSensorPhase.ACTIVE) {
                updateNeighbours(pLevel, pPos, pState);
            }
        }
    }

    @Override
    protected BlockState updateShape(
        BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos
    ) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    private static void updateNeighbours(Level pLevel, BlockPos pPos, BlockState pState) {
        Block block = pState.getBlock();
        pLevel.updateNeighborsAt(pPos, block);
        pLevel.updateNeighborsAt(pPos.below(), block);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SculkSensorBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return !pLevel.isClientSide
            ? createTickerHelper(
                pBlockEntityType,
                BlockEntityType.SCULK_SENSOR,
                (p_281130_, p_281131_, p_281132_, p_281133_) -> VibrationSystem.Ticker.tick(p_281130_, p_281133_.getVibrationData(), p_281133_.getVibrationUser())
            )
            : null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pState.getValue(POWER);
    }

    @Override
    public int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return pDirection == Direction.UP ? pState.getSignal(pLevel, pPos, pDirection) : 0;
    }

    public static SculkSensorPhase getPhase(BlockState pState) {
        return pState.getValue(PHASE);
    }

    public static boolean canActivate(BlockState pState) {
        return getPhase(pState) == SculkSensorPhase.INACTIVE;
    }

    public static void deactivate(Level pLevel, BlockPos pPos, BlockState pState) {
        pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.COOLDOWN).setValue(POWER, Integer.valueOf(0)), 3);
        pLevel.scheduleTick(pPos, pState.getBlock(), 10);
        updateNeighbours(pLevel, pPos, pState);
    }

    @VisibleForTesting
    public int getActiveTicks() {
        return 30;
    }

    public void activate(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, BlockState pState, int pPower, int pFrequency) {
        pLevel.setBlock(pPos, pState.setValue(PHASE, SculkSensorPhase.ACTIVE).setValue(POWER, Integer.valueOf(pPower)), 3);
        pLevel.scheduleTick(pPos, pState.getBlock(), this.getActiveTicks());
        updateNeighbours(pLevel, pPos, pState);
        tryResonateVibration(pEntity, pLevel, pPos, pFrequency);
        pLevel.gameEvent(pEntity, GameEvent.SCULK_SENSOR_TENDRILS_CLICKING, pPos);
        if (!pState.getValue(WATERLOGGED)) {
            pLevel.playSound(
                null,
                (double)pPos.getX() + 0.5,
                (double)pPos.getY() + 0.5,
                (double)pPos.getZ() + 0.5,
                SoundEvents.SCULK_CLICKING,
                SoundSource.BLOCKS,
                1.0F,
                pLevel.random.nextFloat() * 0.2F + 0.8F
            );
        }
    }

    public static void tryResonateVibration(@Nullable Entity pEntity, Level pLevel, BlockPos pPos, int pFrequency) {
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pPos.relative(direction);
            BlockState blockstate = pLevel.getBlockState(blockpos);
            if (blockstate.is(BlockTags.VIBRATION_RESONATORS)) {
                pLevel.gameEvent(VibrationSystem.getResonanceEventByFrequency(pFrequency), blockpos, GameEvent.Context.of(pEntity, blockstate));
                float f = RESONANCE_PITCH_BEND[pFrequency];
                pLevel.playSound(null, blockpos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.0F, f);
            }
        }
    }

    @Override
    public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
        if (getPhase(pState) == SculkSensorPhase.ACTIVE) {
            Direction direction = Direction.getRandom(pRandom);
            if (direction != Direction.UP && direction != Direction.DOWN) {
                double d0 = (double)pPos.getX()
                    + 0.5
                    + (direction.getStepX() == 0 ? 0.5 - pRandom.nextDouble() : (double)direction.getStepX() * 0.6);
                double d1 = (double)pPos.getY() + 0.25;
                double d2 = (double)pPos.getZ()
                    + 0.5
                    + (direction.getStepZ() == 0 ? 0.5 - pRandom.nextDouble() : (double)direction.getStepZ() * 0.6);
                double d3 = (double)pRandom.nextFloat() * 0.04;
                pLevel.addParticle(DustColorTransitionOptions.SCULK_TO_REDSTONE, d0, d1, d2, 0.0, d3, 0.0);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(PHASE, POWER, WATERLOGGED);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState pState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        if (pLevel.getBlockEntity(pPos) instanceof SculkSensorBlockEntity sculksensorblockentity) {
            return getPhase(pState) == SculkSensorPhase.ACTIVE ? sculksensorblockentity.getLastVibrationFrequency() : 0;
        } else {
            return 0;
        }
    }

    @Override
    protected boolean isPathfindable(BlockState pState, PathComputationType pPathComputationType) {
        return false;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState pState) {
        return true;
    }

    @Override
    protected void spawnAfterBreak(BlockState pState, ServerLevel pLevel, BlockPos pPos, ItemStack pStack, boolean pDropExperience) {
        super.spawnAfterBreak(pState, pLevel, pPos, pStack, pDropExperience);
        if (false && pDropExperience) { // Forge: Moved to getExpDrop
            this.tryDropExperience(pLevel, pPos, pStack, ConstantInt.of(5));
        }
    }

    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader level, RandomSource randomSource, BlockPos pos, int fortuneLevel, int silkTouchLevel) {
       return silkTouchLevel == 0 ? 5 : 0;
    }
}
