package com.edivad.solargeneration.blocks;

import java.util.List;
import java.util.Random;

import com.edivad.solargeneration.Main;
import com.edivad.solargeneration.ModBlocks;
import com.edivad.solargeneration.tile.TileEntityAdvancedSolarPanel;
import com.edivad.solargeneration.tile.TileEntityHardenedSolarPanel;
import com.edivad.solargeneration.tile.TileEntityLeadstoneSolarPanel;
import com.edivad.solargeneration.tile.TileEntityRedstoneSolarPanel;
import com.edivad.solargeneration.tile.TileEntityResonantSolarPanel;
import com.edivad.solargeneration.tile.TileEntitySolarPanel;
import com.edivad.solargeneration.tile.TileEntityUltimateSolarPanel;
import com.edivad.solargeneration.tools.SolarPanelLevel;
import com.edivad.solargeneration.tools.Tooltip;
import com.edivad.solargeneration.tools.inter.IGuiTile;
import com.edivad.solargeneration.tools.inter.IRestorableTileEntity;

import cofh.thermalfoundation.item.ItemWrench;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SolarPanel extends Block implements ITileEntityProvider{
	
	//SolarPanel Level
	// 0-SolarPanelLeadstone
	// 1-SolarPanelHardened
	// 2-SolarPanelRedstone
	// 3-SolarPanelResonant
	// 4-SolarPanelAdvanced
	// 5-SolarPanelUltimate
	private final SolarPanelLevel levelSolarPanel;
	
	public SolarPanel(SolarPanelLevel levelSolarPanel) {
		
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setHardness(5F);
		setResistance(30F);
		setHarvestLevel("pickaxe", 0);
		this.levelSolarPanel = levelSolarPanel;
		
		setRegistryName(getResourceLocation(levelSolarPanel));
		setUnlocalizedName(Main.MODID + "." + getResourceLocation(levelSolarPanel).getResourcePath());
		
		setCreativeTab(Main.solarGenerationTab);
	}
	
	public static ResourceLocation getResourceLocation(SolarPanelLevel levelSolarPanel) {
		switch (levelSolarPanel) 
		{
			case Leadstone : return new ResourceLocation(Main.MODID, "solar_panel_leadstone");
			case Hardened : return new ResourceLocation(Main.MODID, "solar_panel_hardened");
			case Redstone : return new ResourceLocation(Main.MODID, "solar_panel_redstone");
			case Resonant : return new ResourceLocation(Main.MODID, "solar_panel_resonant");
			case Advanced : return new ResourceLocation(Main.MODID, "solar_panel_advanced");
			case Ultimate : return new ResourceLocation(Main.MODID, "solar_panel_ultimate");
			default : return new ResourceLocation(Main.MODID, "solar_panel_leadstone");
		}
	}
	
	public SolarPanelLevel getLevelSolarPanel()
	{
		return this.levelSolarPanel;
	}
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		switch (this.levelSolarPanel) 
		{
			case Leadstone : return Item.getItemFromBlock(ModBlocks.solarPanelLeadstone);
			case Hardened : return Item.getItemFromBlock(ModBlocks.solarPanelHardened);
			case Redstone : return Item.getItemFromBlock(ModBlocks.solarPanelRedstone); 
			case Resonant : return Item.getItemFromBlock(ModBlocks.solarPanelResonant);
			case Advanced : return Item.getItemFromBlock(ModBlocks.solarPanelAdvanced);
			case Ultimate : return Item.getItemFromBlock(ModBlocks.solarPanelUltimate);
			default : return Item.getItemFromBlock(ModBlocks.solarPanelLeadstone);
		}
	}
	
	@Override
	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state)
	{
		switch (this.levelSolarPanel) 
		{
			case Leadstone : return new ItemStack(ModBlocks.solarPanelLeadstone);
			case Hardened : return new ItemStack(ModBlocks.solarPanelHardened);
			case Redstone : return new ItemStack(ModBlocks.solarPanelRedstone); 
			case Resonant : return new ItemStack(ModBlocks.solarPanelResonant);
			case Advanced : return new ItemStack(ModBlocks.solarPanelAdvanced);
			case Ultimate : return new ItemStack(ModBlocks.solarPanelUltimate);
			default : return new ItemStack(ModBlocks.solarPanelLeadstone);
		}
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 1;
	}
	
	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(worldIn.isRemote) 
			return true;
		
		if (playerIn.isSneaking()) {
			if(ItemStack.areItemsEqual(playerIn.getHeldItemMainhand(), ItemWrench.wrenchBasic))
			{	
				dismantleBlock(worldIn, pos);
				return true;
			}
			
		}
		
		TileEntity te = worldIn.getTileEntity(pos);
		if(!(te instanceof IGuiTile))
			return false;
		
		playerIn.openGui(Main.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
		return true;
				
	}
	
	private void dismantleBlock(World worldIn, BlockPos pos) {
        ItemStack itemStack = new ItemStack(this);
        
        TileEntitySolarPanel localTileEntity = (TileEntitySolarPanel) worldIn.getTileEntity(pos);
        int internalEnergy = localTileEntity.getEnergy();
        if (internalEnergy > 0) 
        {
            if (itemStack.getTagCompound() == null) 
            {
                itemStack.setTagCompound(new NBTTagCompound());
            }
            itemStack.getTagCompound().setInteger("energy", internalEnergy);
        }

        worldIn.setBlockToAir(pos);
        
        EntityItem entityItem = new EntityItem(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, itemStack);
        entityItem.motionX = 0;
        entityItem.motionZ = 0;
        worldIn.spawnEntity(entityItem);
    }
	
	@Override
	public void getDrops(NonNullList<ItemStack> result, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		
		TileEntity tileEntity = world.getTileEntity(pos);
		
		//Always check this!!
		if(tileEntity instanceof IRestorableTileEntity)
		{
			ItemStack stack = new ItemStack(Item.getItemFromBlock(this));
			NBTTagCompound tagCompound = new NBTTagCompound();
			((IRestorableTileEntity) tileEntity).writeRestorableToNBT(tagCompound);
			
			stack.setTagCompound(tagCompound);
			result.add(stack);
		}
		else
		{
			super.getDrops(result, world, pos, state, fortune);
		}
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
		if(willHarvest)
			return true;
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		worldIn.setBlockToAir(pos);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		
		TileEntity tileEntity = worldIn.getTileEntity(pos);
		
		//Always check this!!
		if(tileEntity instanceof IRestorableTileEntity)
		{
			NBTTagCompound tagCompound = stack.getTagCompound();
			if(tagCompound != null)
			{
				((IRestorableTileEntity) tileEntity).readRestorableFromNBT(tagCompound);
			}
		}
	}
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		switch (this.levelSolarPanel) 
		{
			case Leadstone : return new TileEntityLeadstoneSolarPanel();
			case Hardened : return new TileEntityHardenedSolarPanel();
			case Redstone : return new TileEntityRedstoneSolarPanel();
			case Resonant : return new TileEntityResonantSolarPanel();
			case Advanced : return new TileEntityAdvancedSolarPanel();
			case Ultimate : return new TileEntityUltimateSolarPanel();
			default : return new TileEntityLeadstoneSolarPanel();
		}
	}
	
	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		Tooltip.showInfoCtrl(stack, tooltip);
		Tooltip.showInfoShift(this.levelSolarPanel, tooltip);
	}	
		
	@SideOnly(Side.CLIENT)
    public void initModel() {
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, new ModelResourceLocation(getRegistryName(), "inventory"));
    }
}