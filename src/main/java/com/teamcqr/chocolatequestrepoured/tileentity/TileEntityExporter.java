package com.teamcqr.chocolatequestrepoured.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityExporter extends TileEntity
{
	public int startX;
	public int startY;
	public int startZ;
	public int endX;
	public int endY;
	public int endZ;
	public String structureName;
	
	public TileEntityExporter(){}
	
	@Override
	 public NBTTagCompound writeToNBT(NBTTagCompound compound)
	 {
		 super.writeToNBT(compound);
		 compound.setInteger("StartX", startX);
		 compound.setInteger("StartY", startY);
		 compound.setInteger("StartZ", startZ);
		 compound.setInteger("EndX", endX);
		 compound.setInteger("EndY", endY);
		 compound.setInteger("EndZ", endZ);
		 compound.setString("StructureName", structureName);
		 return compound;
	} 

	@Override
	public void readFromNBT(NBTTagCompound compound)
	{
		super.readFromNBT(compound);
		compound.getInteger("StartX");
		compound.getInteger("StartY");
		compound.getInteger("StartZ");
		compound.getInteger("EndX");
		compound.getInteger("EndY");
		compound.getInteger("EndZ");
		compound.getString("StructureName");
	}
	
	public void setValues(int sX, int sY, int sZ, int eX, int eY, int eZ, String structName)
	{
		sX = startX;
		sY = startY;
		sZ = startZ;
		eX = endX;
		eY = endY;
		eZ = endZ;
		structName = structureName;
	}
}