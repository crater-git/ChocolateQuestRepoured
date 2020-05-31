package com.teamcqr.chocolatequestrepoured.objects.entity.mobs;

import com.teamcqr.chocolatequestrepoured.factions.EDefaultFaction;
import com.teamcqr.chocolatequestrepoured.objects.entity.EBaseHealths;
import com.teamcqr.chocolatequestrepoured.objects.entity.ECQREntityArmPoses;
import com.teamcqr.chocolatequestrepoured.objects.entity.bases.AbstractEntityCQR;
import com.teamcqr.chocolatequestrepoured.objects.entity.boss.AbstractEntityCQRMageBase;
import com.teamcqr.chocolatequestrepoured.util.IRangedWeapon;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.AbstractIllagerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.Item;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class EntityCQRIllager extends AbstractEntityCQR {

	private static final DataParameter<Boolean> IS_AGGRESSIVE = EntityDataManager.<Boolean>createKey(AbstractEntityCQRMageBase.class, DataSerializers.BOOLEAN);

	public EntityCQRIllager(World worldIn, EntityType<? extends EntityCQRIllager> type) {
		super(worldIn, type);
	}

	@Override
	protected void registerData() {
		super.registerData();

		this.dataManager.register(IS_AGGRESSIVE, false);
	}

	@Override
	public void tick() {
		if (!this.world.isRemote) {
			if (this.getAttackTarget() != null && !this.dataManager.get(IS_AGGRESSIVE)) {
				this.dataManager.set(IS_AGGRESSIVE, true);
				this.setArmPose(ECQREntityArmPoses.HOLDING_ITEM);
			} else if (this.getAttackTarget() == null) {
				this.dataManager.set(IS_AGGRESSIVE, false);
				this.setArmPose(ECQREntityArmPoses.NONE);
			}
		}
		super.tick();
	}

	@Override
	public float getBaseHealth() {
		return EBaseHealths.ILLAGER.getValue();
	}

	@Override
	public EDefaultFaction getDefaultFaction() {
		return EDefaultFaction.ILLAGERS;
	}

	@Override
	protected ResourceLocation getLootTable() {
		return LootTables.ENTITIES_VINDICATION_ILLAGER;
	}

	@Override
	public int getTextureCount() {
		return 2;
	}

	public boolean isAggressive() {
		if (!this.world.isRemote) {
			return this.getAttackTarget() != null;
		}
		return this.dataManager.get(IS_AGGRESSIVE);
	}

	@OnlyIn(Dist.CLIENT)
	public AbstractIllagerEntity.ArmPose getIllagerArmPose() {
		if (this.isAggressive()) {
			if (this.isSpellCharging() && this.isSpellAnimated()) {
				return AbstractIllagerEntity.ArmPose.SPELLCASTING;
			}

			Item active = this.getActiveItemStack().getItem();
			if (active instanceof IRangedWeapon || active instanceof BowItem) {
				return AbstractIllagerEntity.ArmPose.BOW_AND_ARROW;
			}
			return AbstractIllagerEntity.ArmPose.ATTACKING;
		}
		return AbstractIllagerEntity.ArmPose.CROSSED;
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.ILLAGER;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return SoundEvents.ENTITY_PILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_PILLAGER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_PILLAGER_DEATH;
	}

}
