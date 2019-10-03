package edivad.solargeneration.tile;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import edivad.solargeneration.blocks.containers.SolarPanelAdvancedContainer;
import edivad.solargeneration.blocks.containers.SolarPanelHardenedContainer;
import edivad.solargeneration.blocks.containers.SolarPanelLeadstoneContainer;
import edivad.solargeneration.blocks.containers.SolarPanelRedstoneContainer;
import edivad.solargeneration.blocks.containers.SolarPanelResonantContainer;
import edivad.solargeneration.blocks.containers.SolarPanelSignalumContainer;
import edivad.solargeneration.blocks.containers.SolarPanelUltimateContainer;
import edivad.solargeneration.tools.MyEnergyStorage;
import edivad.solargeneration.tools.ProductionSolarPanel;
import edivad.solargeneration.tools.SolarPanelLevel;
import edivad.solargeneration.tools.inter.IRestorableTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

public class TileEntitySolarPanel extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IRestorableTileEntity {

	// Energy
	private LazyOptional<IEnergyStorage> energy = LazyOptional.of(this::createEnergy);
	private MyEnergyStorage energyStorage;
	private int energyGeneration;
	private int maxEnergyOutput;

	private SolarPanelLevel levelSolarPanel;

	public TileEntitySolarPanel(SolarPanelLevel levelSolarPanel, TileEntityType<?> tileEntitySolarPanel)
	{
		super(tileEntitySolarPanel);
		this.levelSolarPanel = levelSolarPanel;
		energyGeneration = (int) Math.pow(8, levelSolarPanel.ordinal());
		maxEnergyOutput = energyGeneration * 2;
	}

	private IEnergyStorage createEnergy()
	{
		return new MyEnergyStorage(maxEnergyOutput, energyGeneration * 1000);
	}

	@Override
	public void tick()
	{
		if(world.isRemote)
			return;

		energy.ifPresent(e -> ((MyEnergyStorage) e).generatePower(currentAmountEnergyProduced()));
		sendEnergy();
	}

	public int currentAmountEnergyProduced()
	{
		return (int) (energyGeneration * ProductionSolarPanel.computeSunIntensity(world, getPos(), getLevelSolarPanel()));
	}

	private SolarPanelLevel getLevelSolarPanel()
	{
		return levelSolarPanel;
	}

	/*private void sendEnergy2()
	{
		if(energyStorage.getEnergyStored() > 0)
		{
			for(int i = 0; (i < Direction.values().length) && (energyStorage.getEnergyStored() > 0); i++)
			{
				Direction facing = Direction.values()[i];
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
				if(tileEntity != null)
				{
					tileEntity.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).ifPresent(handler ->
					{
						if(handler.canReceive())
						{
							int accepted = Math.min(maxEnergyOutput, handler.receiveEnergy(energyStorage.getEnergyStored(), true));
							energyStorage.consumePower(accepted);
							handler.receiveEnergy(accepted, false);
						}
					});
				}
			}
			this.markDirty();
		}
	}*/

	private void sendEnergy()
	{
		energy.ifPresent(energy ->
		{
			AtomicInteger capacity = new AtomicInteger(energy.getEnergyStored());

			for(int i = 0; (i < Direction.values().length) && (capacity.get() > 0); i++)
			{
				Direction facing = Direction.values()[i];
				TileEntity tileEntity = world.getTileEntity(pos.offset(facing));
				if(tileEntity != null)
				{
					tileEntity.getCapability(CapabilityEnergy.ENERGY, facing.getOpposite()).ifPresent(handler ->
					{
						if(handler.canReceive())
						{
							int accepted = Math.min(maxEnergyOutput, handler.receiveEnergy(energyStorage.getEnergyStored(), true));
							capacity.addAndGet(-accepted);
							handler.receiveEnergy(accepted, false);
						}
					});
				}
			}
		});
	}

	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing)
	{
		if(capability == CapabilityEnergy.ENERGY)
		{
			return energy.cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void read(CompoundNBT compound)
	{
		readRestorableFromNBT(compound);
		super.read(compound);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		writeRestorableToNBT(compound);
		return super.write(compound);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readRestorableFromNBT(CompoundNBT tag)
	{
		CompoundNBT energyTag = tag.getCompound("energy");
		energy.ifPresent(h -> ((INBTSerializable<CompoundNBT>) h).deserializeNBT(energyTag));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void writeRestorableToNBT(CompoundNBT tag)
	{
		energy.ifPresent(h ->
		{
			CompoundNBT compound = ((INBTSerializable<CompoundNBT>) h).serializeNBT();
			tag.put("energy", compound);
		});
	}

	@Nullable
	@Override
	public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		switch (levelSolarPanel)
		{
			case Advanced:
				return new SolarPanelAdvancedContainer(id, world, pos, playerEntity);
			case Hardened:
				return new SolarPanelHardenedContainer(id, world, pos, playerEntity);
			case Leadstone:
				return new SolarPanelLeadstoneContainer(id, world, pos, playerEntity);
			case Redstone:
				return new SolarPanelRedstoneContainer(id, world, pos, playerEntity);
			case Resonant:
				return new SolarPanelResonantContainer(id, world, pos, playerEntity);
			case Signalum:
				return new SolarPanelSignalumContainer(id, world, pos, playerEntity);
			case Ultimate:
				return new SolarPanelUltimateContainer(id, world, pos, playerEntity);
			default:
				return null;
		}
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new StringTextComponent("solar_panel_" + levelSolarPanel.name().toLowerCase());
	}
}
