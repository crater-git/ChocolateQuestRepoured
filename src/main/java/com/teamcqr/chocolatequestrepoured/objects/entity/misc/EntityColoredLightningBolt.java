package com.teamcqr.chocolatequestrepoured.objects.entity.misc;

import java.util.List;

import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityColoredLightningBolt extends LightningBoltEntity implements IEntityAdditionalSpawnData {

	/** Declares which state the lightning bolt is in. Whether it's in the air, hit the ground, etc. */
	private int lightningState;
	/** Determines the time before the EntityLightningBolt is destroyed. It is a random integer decremented over time. */
	private int boltLivingTime;
	protected boolean hitEntities;
	protected boolean spreadFire;
	public float red;
	public float green;
	public float blue;
	public float alpha;

	public EntityColoredLightningBolt(World worldIn) {
		this(worldIn, 0.0D, 0.0D, 0.0D, false, false);
	}

	public EntityColoredLightningBolt(World worldIn, double x, double y, double z, boolean hitEntities, boolean spreadFire) {
		this(worldIn, x, y, z, hitEntities, spreadFire, 0.45F, 0.45F, 0.5F, 0.3F);
	}

	/**
	 * Vanilla color is: 0.45F, 0.45F, 0.5F, 0.3F
	 */
	public EntityColoredLightningBolt(World worldIn, double x, double y, double z, boolean hitEntities, boolean spreadFire, float red, float green, float blue, float alpha) {
		super(worldIn, x, y, z, true);
		this.isImmuneToFire = true;
		this.lightningState = 2;
		this.boltLivingTime = this.rand.nextInt(3) + 1;
		this.hitEntities = hitEntities;
		this.spreadFire = spreadFire;
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
		BlockPos blockpos = new BlockPos(this);

		if (spreadFire && !worldIn.isRemote && worldIn.getGameRules().getBoolean(GameRules.DO_FIRE_TICK) && (worldIn.getDifficulty() == Difficulty.NORMAL || worldIn.getDifficulty() == Difficulty.HARD) && worldIn.isAreaLoaded(blockpos, 10)) {
			if (worldIn.getBlockState(blockpos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos)) {
				worldIn.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
			}

			for (int i = 0; i < 4; i++) {
				BlockPos blockpos1 = blockpos.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);

				if (worldIn.getBlockState(blockpos1).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(worldIn, blockpos1)) {
					worldIn.setBlockState(blockpos1, Blocks.FIRE.getDefaultState());
				}
			}
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass == 1;
	}

	@Override
	public void tick() {
		if (!this.world.isRemote) {
			this.setFlag(6, this.isGlowing());
		}

		this.onEntityUpdate();

		if (this.lightningState == 2) {
			this.world.playSound((PlayerEntity) null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
			this.world.playSound((PlayerEntity) null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
		}

		--this.lightningState;

		if (this.lightningState < 0) {
			if (this.boltLivingTime == 0) {
				this.remove();
			} else if (this.lightningState < -this.rand.nextInt(10)) {
				--this.boltLivingTime;
				this.lightningState = 1;

				if (this.spreadFire && !this.world.isRemote) {
					this.boltVertex = this.rand.nextLong();
					BlockPos blockpos = new BlockPos(this);

					if (this.world.getGameRules().getBoolean(GameRules.DO_FIRE_TICK) && this.world.isAreaLoaded(blockpos, 10) && this.world.getBlockState(blockpos).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(this.world, blockpos)) {
						this.world.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
					}
				}
			}
		}

		if (this.lightningState >= 0) {
			if (this.world.isRemote) {
				this.world.setLastLightningBolt(2);
			} else if (this.hitEntities) {
				AxisAlignedBB aabb = new AxisAlignedBB(this.getPosX() - 3.0D, this.getPosY() - 3.0D, this.getPosZ() - 3.0D, this.getPosX() + 3.0D, this.getPosY() + 6.0D + 3.0D, this.getPosZ() + 3.0D);
				List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, aabb);

				for (Entity entity : list) {
					if (!ForgeEventFactory.onEntityStruckByLightning(entity, this)) {
						entity.onStruckByLightning(this);
					}
				}
			}
		}
	}

	@Override
	protected void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);
		compound.putFloat("red", this.red);
		compound.putFloat("green", this.green);
		compound.putFloat("blue", this.blue);
		compound.putFloat("alpha", this.alpha);
	}

	@Override
	protected void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);
		this.red = compound.getFloat("red");
		this.green = compound.getFloat("green");
		this.blue = compound.getFloat("blue");
		this.alpha = compound.getFloat("alpha");
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeFloat(this.red);
		buffer.writeFloat(this.green);
		buffer.writeFloat(this.blue);
		buffer.writeFloat(this.alpha);
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.red = additionalData.readFloat();
		this.green = additionalData.readFloat();
		this.blue = additionalData.readFloat();
		this.alpha = additionalData.readFloat();
	}

}
