package edivad.solargeneration.blocks;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import edivad.solargeneration.tile.TileEntitySolarPanel;
import edivad.solargeneration.tools.SolarPanelLevel;
import edivad.solargeneration.tools.Tooltip;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.NetworkHooks;

public class SolarPanel extends Block implements IWaterLoggable {

    private final SolarPanelLevel levelSolarPanel;
    private static final VoxelShape BOX = createShape();
    //https://twitter.com/McJty/status/1251439077787869188
    private static final ResourceLocation WRENCH = new ResourceLocation("forge", "wrench");
    private static final BooleanProperty WATERLOGGED = BooleanProperty.create("waterlogged");

    public SolarPanel(SolarPanelLevel levelSolarPanel)
    {
        super(Properties.create(Material.IRON).sound(SoundType.METAL).hardnessAndResistance(5F, 30F));
        this.setDefaultState(getDefaultState().with(WATERLOGGED, false));
        this.levelSolarPanel = levelSolarPanel;
    }

    private static VoxelShape createShape()
    {
        ArrayList<VoxelShape> shapes = new ArrayList<>();
        shapes.add(makeCuboidShape(0, 0, 0, 16, 1, 16));//bottom
        shapes.add(makeCuboidShape(7, 1, 7, 9, 9, 9));//mainpillar
        shapes.add(makeCuboidShape(6, 1, 9, 7, 9, 10));//pillar1
        shapes.add(makeCuboidShape(9, 1, 9, 10, 9, 10));//pillar2
        shapes.add(makeCuboidShape(9, 1, 6, 10, 9, 7));//pillar3
        shapes.add(makeCuboidShape(6, 1, 6, 7, 9, 7));//pillar4
        shapes.add(makeCuboidShape(0, 9, 0, 16, 12, 16));//top

        VoxelShape combinedShape = VoxelShapes.empty();
        for(VoxelShape shape : shapes)
        {
            combinedShape = VoxelShapes.combine(combinedShape, shape, IBooleanFunction.OR);
        }
        return combinedShape;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return BOX;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
    {
        return BOX;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if(!worldIn.isRemote)
        {
            if(player.isCrouching())
            {
                if(player.getHeldItemMainhand().getItem().getTags().contains(WRENCH))
                {
                    dismantleBlock(worldIn, pos);
                    return ActionResultType.SUCCESS;
                }
            }

            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof INamedContainerProvider)
            {
                NetworkHooks.openGui((ServerPlayerEntity) player, (INamedContainerProvider) tileEntity, tileEntity.getPos());
            }
            else
            {
                throw new IllegalStateException("Our named container provider is missing!");
            }
        }
        return ActionResultType.SUCCESS;
    }

    private void dismantleBlock(World worldIn, BlockPos pos)
    {
        ItemStack itemStack = new ItemStack(this);

        TileEntitySolarPanel localTileEntity = (TileEntitySolarPanel) worldIn.getTileEntity(pos);
        int internalEnergy = localTileEntity.getCapability(CapabilityEnergy.ENERGY).map(IEnergyStorage::getEnergyStored).orElse(0);
        if(internalEnergy > 0)
        {
            CompoundNBT energyValue = new CompoundNBT();
            energyValue.putInt("value", internalEnergy);

            CompoundNBT energy = new CompoundNBT();
            energy.put("energy", energyValue);

            CompoundNBT root = new CompoundNBT();
            root.put("BlockEntityTag", energy);
            itemStack.setTag(root);
        }

        worldIn.removeBlock(pos, false);

        ItemEntity entityItem = new ItemEntity(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);

        entityItem.setMotion(0, entityItem.getYOffset(), 0);
        worldIn.addEntity(entityItem);
    }

    @Override
    public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid)
    {
        return willHarvest || super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }

    @Override
    public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack)
    {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.removeBlock(pos, false);
    }

    @Override
    public boolean hasTileEntity(BlockState state)
    {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world)
    {
        return new TileEntitySolarPanel(levelSolarPanel);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn)
    {
        CompoundNBT compoundnbt = stack.getChildTag("BlockEntityTag");
        int energy = 0;
        if(compoundnbt != null)
            if(compoundnbt.contains("energy"))
                energy = compoundnbt.getCompound("energy").getInt("value");

        Tooltip.showInfoCtrl(energy, tooltip);
        Tooltip.showInfoShift(this.levelSolarPanel, tooltip);
    }

    @SuppressWarnings("deprecation")
    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    public boolean receiveFluid(IWorld worldIn, BlockPos pos, BlockState state, FluidState fluidStateIn)
    {
        return IWaterLoggable.super.receiveFluid(worldIn, pos, state, fluidStateIn);
    }

    @Override
    public boolean canContainFluid(IBlockReader worldIn, BlockPos pos, BlockState state, Fluid fluidIn)
    {
        return IWaterLoggable.super.canContainFluid(worldIn, pos, state, fluidIn);
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder)
    {
        super.fillStateContainer(builder);
        builder.add(WATERLOGGED);
    }
}
