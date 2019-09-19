package com.teamcqr.chocolatequestrepoured.objects.entity.mobs;

import com.teamcqr.chocolatequestrepoured.factions.EFaction;
import com.teamcqr.chocolatequestrepoured.objects.entity.EBaseHealths;
import com.teamcqr.chocolatequestrepoured.objects.entity.bases.AbstractEntityCQR;

import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

public class EntityCQRTriton extends AbstractEntityCQR {

	public EntityCQRTriton(World worldIn) {
		super(worldIn);
	}

	@Override
	protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {

	}

	@Override
	public float getBaseHealth() {
		return EBaseHealths.TRITON.getValue();
	}

	@Override
	public EFaction getFaction() {
		return EFaction.TRITONS;
	}

}