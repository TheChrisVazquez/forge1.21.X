package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock extends Block implements BucketPickup {
    private static final Codec<FlowingFluid> FLOWING_FLUID = BuiltInRegistries.FLUID
        .byNameCodec()
        .comapFlatMap(
            p_309784_ -> p_309784_ instanceof FlowingFluid flowingfluid
                    ? DataResult.success(flowingfluid)
                    : DataResult.error(() -> "Not a flowing fluid: " + p_309784_),
            p_311315_ -> (Fluid)p_311315_
        );
    public static final MapCodec<LiquidBlock> CODEC = RecordCodecBuilder.mapCodec(
        p_310157_ -> p_310157_.group(FLOWING_FLUID.fieldOf("fluid").forGetter(p_312827_ -> p_312827_.fluid), propertiesCodec()).apply(p_310157_, LiquidBlock::new)
    );
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    @Deprecated // Use getFluid
    private final FlowingFluid fluid;
    private final List<FluidState> stateCache;
    public static final VoxelShape STABLE_SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.SOUTH, Direction.NORTH, Direction.EAST, Direction.WEST);

    @Override
    public MapCodec<LiquidBlock> codec() {
        return CODEC;
    }

    @Deprecated  // Forge: Use the constructor that takes a supplier
    public LiquidBlock(FlowingFluid p_54694_, BlockBehaviour.Properties p_54695_) {
        super(p_54695_);
        this.fluid = p_54694_;
        this.stateCache = Lists.newArrayList();
        this.stateCache.add(p_54694_.getSource(false));

        for (int i = 1; i < 8; i++) {
            this.stateCache.add(p_54694_.getFlowing(8 - i, false));
        }

        this.stateCache.add(p_54694_.getFlowing(8, true));
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
        fluidStateCacheInitialized = true;
        supplier = () -> p_54694_;
    }

    /**
     * @param p_54694_ A fluid supplier such as {@link net.minecraftforge.registries.RegistryObject<FlowingFluid>}
     */
    public LiquidBlock(java.util.function.Supplier<? extends FlowingFluid> p_54694_, BlockBehaviour.Properties p_54695_) {
        super(p_54695_);
        this.fluid = null;
        this.stateCache = Lists.newArrayList();
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, Integer.valueOf(0)));
        this.supplier = p_54694_;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pContext.isAbove(STABLE_SHAPE, pPos, true)
                && pState.getValue(LEVEL) == 0
                && pContext.canStandOnFluid(pLevel.getFluidState(pPos.above()), pState.getFluidState())
            ? STABLE_SHAPE
            : Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState pState) {
        return pState.getFluidState().isRandomlyTicking();
    }

    @Override
    protected void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        pState.getFluidState().randomTick(pLevel, pPos, pRandom);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState pState, PathComputationType pPathComputationType) {
        return !this.fluid.is(FluidTags.LAVA);
    }

    @Override
    protected FluidState getFluidState(BlockState pState) {
        int i = pState.getValue(LEVEL);
        if (!fluidStateCacheInitialized) initFluidStateCache();
        return this.stateCache.get(Math.min(i, 8));
    }

    @Override
    protected boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return pAdjacentBlockState.getFluidState().getType().isSame(this.fluid);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState pState, LootParams.Builder pParams) {
        return Collections.emptyList();
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (!net.minecraftforge.fluids.FluidInteractionRegistry.canInteract(pLevel, pPos)) {
            pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
        }
    }

    @Override
    protected BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        if (pState.getFluidState().isSource() || pFacingState.getFluidState().isSource()) {
            pLevel.scheduleTick(pCurrentPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    protected void neighborChanged(BlockState pState, Level pLevel, BlockPos pPos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!net.minecraftforge.fluids.FluidInteractionRegistry.canInteract(pLevel, pPos)) {
            pLevel.scheduleTick(pPos, pState.getFluidState().getType(), this.fluid.getTickDelay(pLevel));
        }
    }

    @Deprecated // FORGE: Use FluidInteractionRegistry#canInteract instead
    private boolean shouldSpreadLiquid(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.fluid.is(FluidTags.LAVA)) {
            boolean flag = pLevel.getBlockState(pPos.below()).is(Blocks.SOUL_SOIL);

            for (Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
                BlockPos blockpos = pPos.relative(direction.getOpposite());
                if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                    Block block = pLevel.getFluidState(pPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
                    pLevel.setBlockAndUpdate(pPos, block.defaultBlockState());
                    this.fizz(pLevel, pPos);
                    return false;
                }

                if (flag && pLevel.getBlockState(blockpos).is(Blocks.BLUE_ICE)) {
                    pLevel.setBlockAndUpdate(pPos, Blocks.BASALT.defaultBlockState());
                    this.fizz(pLevel, pPos);
                    return false;
                }
            }
        }

        return true;
    }

    private void fizz(LevelAccessor pLevel, BlockPos pPos) {
        pLevel.levelEvent(1501, pPos, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LEVEL);
    }

    @Override
    public ItemStack pickupBlock(@Nullable Player pPlayer, LevelAccessor pLevel, BlockPos pPos, BlockState pState) {
        if (pState.getValue(LEVEL) == 0) {
            pLevel.setBlock(pPos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.fluid.getBucket());
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return this.fluid.getPickupSound();
    }

    private final java.util.function.Supplier<? extends net.minecraft.world.level.material.Fluid> supplier;
    public FlowingFluid getFluid() {
        return (FlowingFluid)supplier.get();
    }

    private boolean fluidStateCacheInitialized = false;
    protected synchronized void initFluidStateCache() {
        if (fluidStateCacheInitialized == false) {
            this.stateCache.add(getFluid().getSource(false));

            for (int i = 1; i < 8; ++i) {
                this.stateCache.add(getFluid().getFlowing(8 - i, false));
            }

            this.stateCache.add(getFluid().getFlowing(8, true));
            fluidStateCacheInitialized = true;
        }
    }
}
