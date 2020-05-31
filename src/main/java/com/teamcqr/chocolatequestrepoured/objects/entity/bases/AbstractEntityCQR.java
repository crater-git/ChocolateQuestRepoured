package com.teamcqr.chocolatequestrepoured.objects.entity.bases;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import com.github.alexthe666.citadel.server.message.PacketBufferUtils;
import com.teamcqr.chocolatequestrepoured.CQRMain;
import com.teamcqr.chocolatequestrepoured.capability.extraitemhandler.CapabilityExtraItemHandler;
import com.teamcqr.chocolatequestrepoured.capability.extraitemhandler.CapabilityExtraItemHandlerProvider;
import com.teamcqr.chocolatequestrepoured.client.init.ESpeechBubble;
import com.teamcqr.chocolatequestrepoured.client.render.entity.layers.LayerCQRSpeechbubble;
import com.teamcqr.chocolatequestrepoured.factions.CQRFaction;
import com.teamcqr.chocolatequestrepoured.factions.EDefaultFaction;
import com.teamcqr.chocolatequestrepoured.factions.FactionRegistry;
import com.teamcqr.chocolatequestrepoured.init.ModItems;
import com.teamcqr.chocolatequestrepoured.init.ModSounds;
import com.teamcqr.chocolatequestrepoured.network.packets.toClient.ItemStackSyncPacket;
import com.teamcqr.chocolatequestrepoured.objects.entity.ECQREntityArmPoses;
import com.teamcqr.chocolatequestrepoured.objects.entity.EntityEquipmentExtraSlot;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIAttack;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIAttackRanged;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIBackstab;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIFireFighter;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIFollowAttackTarget;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIFollowPath;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIHealingPotion;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIIdleSit;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIMoveToHome;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIMoveToLeader;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAIPotionThrower;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAISearchMount;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAITameAndLeashPet;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.EntityAITorchIgniter;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.EntityAISpellHandler;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.IEntityAISpellAnimatedVanilla;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.target.EntityAICQRNearestAttackTarget;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.target.EntityAIHurtByTarget;
import com.teamcqr.chocolatequestrepoured.objects.factories.SpawnerFactory;
import com.teamcqr.chocolatequestrepoured.objects.items.ItemBadge;
import com.teamcqr.chocolatequestrepoured.objects.items.ItemPotionHealing;
import com.teamcqr.chocolatequestrepoured.objects.items.ItemShieldDummy;
import com.teamcqr.chocolatequestrepoured.objects.items.staves.ItemStaffHealing;
import com.teamcqr.chocolatequestrepoured.structuregen.EDungeonMobType;
import com.teamcqr.chocolatequestrepoured.util.CQRConfig;
import com.teamcqr.chocolatequestrepoured.util.ItemUtil;
import com.teamcqr.chocolatequestrepoured.util.Reference;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ILivingEntityData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.OpenDoorGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.GroundPathNavigator;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public abstract class AbstractEntityCQR extends CreatureEntity implements IMob, IEntityAdditionalSpawnData {

	protected BlockPos homePosition = null;
	protected UUID leaderUUID;
	protected LivingEntity leader = null;
	protected boolean holdingPotion;
	protected ResourceLocation lootTable;
	protected byte usedPotions = (byte) 0;
	protected double healthScale = 1D;
	public ItemStack prevPotion;
	public boolean prevSneaking;
	public boolean prevSitting;
	protected float sizeScaling = 1.0F;
	protected int lastTimeSeenAttackTarget;
	protected Vec3d lastPosAttackTarget;
	protected EntityAISpellHandler spellHandler;

	private CQRFaction factionInstance;
	private String factionName;
	private CQRFaction defaultFactionInstance;

	protected int lastTimeHitByAxeWhileBlocking = 0;
	protected boolean wasRecentlyHitByAxe = false;
	protected boolean armorActive = false;
	protected int magicArmorCooldown = 300;

	// Pathing AI stuff
	protected BlockPos[] pathPoints = new BlockPos[] {};
	protected boolean pathIsLoop = false;
	protected int currentTargetPoint = 0;

	// Sync with client
	protected static final DataParameter<Boolean> IS_SITTING = EntityDataManager.<Boolean>createKey(AbstractEntityCQR.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Boolean> HAS_TARGET = EntityDataManager.<Boolean>createKey(AbstractEntityCQR.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<String> ARM_POSE = EntityDataManager.<String>createKey(AbstractEntityCQR.class, DataSerializers.STRING);
	protected static final DataParameter<Boolean> TALKING = EntityDataManager.<Boolean>createKey(AbstractEntityCQR.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Integer> TEXTURE_INDEX = EntityDataManager.<Integer>createKey(AbstractEntityCQR.class, DataSerializers.VARINT);
	protected static final DataParameter<Boolean> MAGIC_ARMOR_ACTIVE = EntityDataManager.<Boolean>createKey(AbstractEntityCQR.class, DataSerializers.BOOLEAN);
	protected static final DataParameter<Integer> SPELL_INFORMATION = EntityDataManager.<Integer>createKey(AbstractEntityCQR.class, DataSerializers.VARINT);
	//Shoulder entity stuff
	protected static final DataParameter<CompoundNBT> SHOULDER_ENTITY = EntityDataManager.<CompoundNBT>createKey(AbstractEntityCQR.class, DataSerializers.COMPOUND_NBT);

	public int deathTicks = 0;
	public static float MAX_DEATH_TICKS = 200.0F;

	// Client only
	@OnlyIn(Dist.CLIENT)
	protected int currentSpeechBubbleID;

	public AbstractEntityCQR(World worldIn, EntityType<? extends AbstractEntityCQR> type) {
		super(type, worldIn);
		if (worldIn.isRemote) {
			this.currentSpeechBubbleID = this.getRNG().nextInt(ESpeechBubble.values().length);
		}
		this.experienceValue = 5;
		this.setSize(this.getDefaultWidth(), this.getDefaultHeight());
	}

	@Override
	protected void registerData() {
		super.registerData();

		this.dataManager.register(IS_SITTING, false);
		this.dataManager.register(HAS_TARGET, false);
		this.dataManager.register(ARM_POSE, ECQREntityArmPoses.NONE.toString());
		this.dataManager.register(TALKING, false);
		this.dataManager.register(TEXTURE_INDEX, this.getRNG().nextInt(this.getTextureCount()));
		this.dataManager.register(MAGIC_ARMOR_ACTIVE, false);
		this.dataManager.register(SPELL_INFORMATION, 0);
		
		//Shoulder entity stuff
		this.dataManager.register(SHOULDER_ENTITY, new CompoundNBT());
	}

	protected boolean canDespawn() {
		return !CQRConfig.general.mobsFromCQSpawnerDontDespawn;
	}
	
	@Override
	public boolean canDespawn(double distanceToClosestPlayer) {
		return super.canDespawn(distanceToClosestPlayer) || this.canDespawn();
	}

	@Override
	protected void registerAttributes() {
		super.registerAttributes();
		this.getAttributes().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
		this.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.25D);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.getBaseHealth());
	}

	@Override
	protected PathNavigator createNavigator(World worldIn) {
		PathNavigator navigator = new GroundPathNavigator(this, worldIn) /*{
			@Override
			public float getPathSearchRange() {
				return 256.0F;
			}
		}*/;
		((GroundPathNavigator) navigator).setEnterDoors(this.canOpenDoors());
		navigator.setRangeMultiplier(20);
		((GroundPathNavigator) navigator).setBreakDoors(this.canOpenDoors());
		return navigator;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount) {
		return this.attackEntityFrom(source, amount, false);
	}

	public boolean attackEntityFrom(DamageSource source, float amount, boolean sentFromPart) {
		// Start IceAndFire compatibility
		if (CQRConfig.advanced.enableSpecialFeatures && source.getTrueSource() != null) {
			ResourceLocation resLoc = source.getTrueSource().getType().getRegistryName();
			if (resLoc != null && resLoc.getNamespace().equalsIgnoreCase("iceandfire")) {
				amount *= 0.5F;
			}
		}
		// End IceAndFire compatibility

		//Shoulder entity stuff
		spawnShoulderEntities();
		
		if (this.world.getWorldInfo().isHardcore()) {
			amount *= 0.7F;
		} else {
			Difficulty difficulty = this.world.getDifficulty();
			if (difficulty == Difficulty.HARD) {
				amount *= 0.8F;
			} else if (difficulty == Difficulty.NORMAL) {
				amount *= 0.9F;
			}
		}

		if (CQRConfig.mobs.blockCancelledByAxe && !this.world.isRemote && amount > 0.0F && this.canBlockDamageSource(source) && source.getImmediateSource() instanceof LivingEntity && !(source.getImmediateSource() instanceof PlayerEntity) && ((LivingEntity) source.getImmediateSource()).getHeldItemMainhand().getItem() instanceof AxeItem) {
			this.lastTimeHitByAxeWhileBlocking = this.ticksExisted;
		}

		if (super.attackEntityFrom(source, amount)) {
			if (CQRConfig.mobs.armorShattersOnMobs) {
				this.handleArmorBreaking();
			}
			
			return true;
		}

		return false;
	}

	public boolean canBlockDamageSource(DamageSource damageSourceIn) {
		if (!damageSourceIn.isUnblockable() && this.isActiveItemStackBlocking()) {
			Vec3d vec3d = damageSourceIn.getDamageLocation();

			if (vec3d != null) {
				Vec3d vec3d1 = this.getLook(1.0F);
				Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.getPosX(), this.getPosY(), this.getPosZ())).normalize();
				vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

				if (vec3d2.dotProduct(vec3d1) < 0.0D) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void onDeath(DamageSource cause) {
		if (this.isHoldingPotion()) {
			this.swapWeaponAndPotionSlotItemStacks();
		}

		super.onDeath(cause);

		this.updateReputationOnDeath(cause);
	}

	
	@Override
	protected void registerGoals() {
		this.spellHandler = this.createSpellHandler();
		this.goalSelector.addGoal(0, new SwimGoal(this));
		this.goalSelector.addGoal(1, new OpenDoorGoal(this, true) {
			@Override
			public boolean shouldExecute() {
				return AbstractEntityCQR.this.canOpenDoors() && super.shouldExecute();
			}
		});

		this.goalSelector.addGoal(10, new EntityAIHealingPotion(this));
		this.goalSelector.addGoal(11, this.spellHandler);
		this.goalSelector.addGoal(12, new EntityAIAttackRanged(this));
		this.goalSelector.addGoal(12, new EntityAIPotionThrower(this));
		this.goalSelector.addGoal(13, new EntityAIBackstab(this));
		this.goalSelector.addGoal(14, new EntityAIAttack(this));

		this.goalSelector.addGoal(20, new EntityAIFollowAttackTarget(this));
		this.goalSelector.addGoal(21, new EntityAIFireFighter(this));
		this.goalSelector.addGoal(22, new EntityAITorchIgniter(this));
		this.goalSelector.addGoal(23, new EntityAITameAndLeashPet(this));
		this.goalSelector.addGoal(24, new EntityAISearchMount(this));

		this.goalSelector.addGoal(30, new EntityAIMoveToLeader(this));
		this.goalSelector.addGoal(31, new EntityAIFollowPath(this));
		this.goalSelector.addGoal(32, new EntityAIMoveToHome(this));
		this.goalSelector.addGoal(33, new EntityAIIdleSit(this));

		this.targetSelector.addGoal(0, new EntityAICQRNearestAttackTarget(this));
		this.targetSelector.addGoal(1, new EntityAIHurtByTarget(this));
	}

	@Override
	@Nullable
	public ILivingEntityData onInitialSpawn(IWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, ILivingEntityData livingdata, CompoundNBT dataTag) {
		this.setHealingPotions(CQRConfig.mobs.defaultHealingPotionCount);
		this.setItemStackToExtraSlot(EntityEquipmentExtraSlot.BADGE, new ItemStack(ModItems.BADGE));
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			this.setDropChance(slot, 0.04F);
		}
		return livingdata;
	}

	@Override
	protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
		double modalValue = CQRConfig.mobs.dropDurabilityModalValue;
		double standardDeviation = CQRConfig.mobs.dropDurabilityStandardDeviation;
		double min = Math.min(CQRConfig.mobs.dropDurabilityMinimum, modalValue);
		double max = Math.max(CQRConfig.mobs.dropDurabilityMaximum, modalValue);

		for (EquipmentSlotType entityequipmentslot : EquipmentSlotType.values()) {
			ItemStack itemstack = this.getItemStackFromSlot(entityequipmentslot);
			double d0 = (double) this.getDropChance(entityequipmentslot);
			boolean flag = d0 > 1.0D;

			if (!itemstack.isEmpty() && !EnchantmentHelper.hasVanishingCurse(itemstack) && (wasRecentlyHit || flag) && (double) (this.rand.nextFloat() - (float) lootingModifier * 0.01F) < d0) {
				if (!flag && itemstack.isItemStackDamageable()) {
					double durability = modalValue + MathHelper.clamp(this.rand.nextGaussian() * standardDeviation, min - modalValue, max - modalValue);
					itemstack.setItemDamage((int) ((double) itemstack.getMaxDamage() * (1.0D - durability)));
				}

				this.entityDropItem(itemstack, 0.0F);
			}
		}
	}

	@Override
	public void writeAdditional(CompoundNBT compound) {
		super.writeAdditional(compound);

		if (this.homePosition != null) {
			compound.put("home", NBTUtil.writeBlockPos(this.homePosition));
		}

		if (this.leaderUUID != null) {
			compound.put("leader", NBTUtil.writeUniqueId(this.leaderUUID));
		}
		if (this.factionName != null && !this.factionName.equalsIgnoreCase(this.getDefaultFaction().name())) {
			compound.putString("factionOverride", this.factionName);
		}
		compound.putInt("textureIndex", this.dataManager.get(TEXTURE_INDEX));
		compound.putByte("usedHealingPotions", this.usedPotions);
		compound.putFloat("sizeScaling", this.sizeScaling);
		compound.putBoolean("isSitting", this.dataManager.get(IS_SITTING));
		compound.putBoolean("holdingPotion", this.holdingPotion);
		compound.putDouble("healthScale", this.healthScale);

		if (this.pathPoints.length > 0) {
			CompoundNBT pathTag = new CompoundNBT();
			pathTag.putBoolean("isLoop", this.pathIsLoop);
			pathTag.putInt("currentPathPoint", this.currentTargetPoint);
			ListNBT nbtTagList = new ListNBT();
			for (int i = 0; i < this.pathPoints.length; i++) {
				nbtTagList.add(NBTUtil.writeBlockPos(this.pathPoints[i]));
			}
			pathTag.put("pathPoints", nbtTagList);
			compound.put("pathingAI", pathTag);
		}
		
		//Shoulder entity stuff
		if (!this.getLeftShoulderEntity().isEmpty())
        {
            compound.put("ShoulderEntityLeft", this.getLeftShoulderEntity());
        }

	}
	
	@Override
	public void readAdditional(CompoundNBT compound) {
		super.readAdditional(compound);

		if (compound.contains("home")) {
			this.homePosition = NBTUtil.readBlockPos(compound.getCompound("home"));
		}

		if (compound.contains("leader")) {
			this.leaderUUID = NBTUtil.readUniqueId(compound.getCompound("leader"));
		}

		if (compound.contains("factionOverride")) {
			this.setFaction(compound.getString("factionOverride"));
		}

		this.dataManager.set(TEXTURE_INDEX, compound.getInt("textureIndex"));
		this.usedPotions = compound.getByte("usedHealingPotions");
		this.sizeScaling = compound.contains("sizeScaling") ? compound.getFloat("sizeScaling") : 1.0F;
		this.dataManager.set(IS_SITTING, compound.getBoolean("isSitting"));
		this.holdingPotion = compound.getBoolean("holdingPotion");
		this.healthScale = compound.getDouble("healthScale");
		if (this.healthScale <= 1.0D) {
			this.healthScale = 1.0D;
		}

		if (compound.contains("pathingAI", Constants.NBT.TAG_COMPOUND)) {
			CompoundNBT pathTag = compound.getCompound("pathingAI");
			this.pathIsLoop = pathTag.getBoolean("isLoop");
			this.currentTargetPoint = pathTag.getInt("currentPathPoint") -1;
			ListNBT nbtTagList = pathTag.getList("pathPoints", Constants.NBT.TAG_COMPOUND);
			this.pathPoints = new BlockPos[nbtTagList.size()];
			for (int i = 0; i < nbtTagList.size(); i++) {
				this.pathPoints[i] = NBTUtil.readBlockPos(nbtTagList.getCompound(i));
			}
		}
		
		//Shoulder entity stuff
		if (compound.contains("ShoulderEntityLeft", 10))
        {
            this.setLeftShoulderEntity(compound.getCompound("ShoulderEntityLeft"));
        }

	}

	@Override
	protected boolean processInteract(PlayerEntity player, Hand hand) {
		if (player.isCreative() && !player.isSneaking()) {
			if (!this.world.isRemote) {
				ItemStack stack = player.getHeldItem(hand);

				if (stack.getItem() instanceof ArmorItem) {
					EquipmentSlotType slot = getSlotForItemStack(stack);

					player.setHeldItem(hand, this.getItemStackFromSlot(slot));
					this.setItemStackToSlot(slot, stack);
					return true;
				}

				if (stack.getItem() instanceof SwordItem) {
					player.setHeldItem(hand, this.getHeldItemMainhand());
					this.setHeldItem(Hand.MAIN_HAND, stack);
					return true;
				}

				if (stack.getItem() instanceof ShieldItem) {
					player.setHeldItem(hand, this.getHeldItemOffhand());
					this.setHeldItem(Hand.OFF_HAND, stack);
					return true;
				}

				if (!this.getLookController().getIsLooking() && !this.hasPath()) {
					double x1 = player.getPosX() - this.getPosX();
					double z1 = player.getPosZ() - this.getPosZ();
					float yaw = (float) Math.toDegrees(Math.atan2(-x1, z1));
					this.rotationYaw = yaw;
					this.rotationYawHead = yaw;
					this.renderYawOffset = yaw;
				}
				player.openGui(CQRMain.INSTANCE, Reference.CQR_ENTITY_GUI_ID, this.world, this.getEntityId(), 0, 0);
			}
			return true;
		}
		if (this.hasLeader() && this.getLeader() == player && !player.isSneaking()) {
			if (!this.world.isRemote) {
				player.openGui(CQRMain.INSTANCE, Reference.CQR_ENTITY_GUI_ID, this.world, this.getEntityId(), 0, 0);
			}
			return true;
		}
		return false;
	}

	@Override
	protected abstract ResourceLocation getLootTable();

	
	@Override
	protected void dropSpecialItems(DamageSource source, int looting, boolean wasRecentlyHit) {
		ResourceLocation resourcelocation = this.getLootTable();
		if (resourcelocation != null) {
			LootTable lootTable = this.world.getLootTableManager().getLootTableFromLocation(resourcelocation);
			LootContext.Builder lootContextBuilder = new LootContext.Builder((WorldServer) this.world).withLootedEntity(this).withDamageSource(source);
			if (wasRecentlyHit && this.attackingPlayer != null) {
				lootContextBuilder = lootContextBuilder.withPlayer(this.attackingPlayer).withLuck(this.attackingPlayer.getLuck());
			}

			for (ItemStack itemstack : lootTable.generateLootForPools(this.rand, lootContextBuilder.build())) {
				this.entityDropItem(itemstack, 0.0F);
			}
		}

		ItemStack badge = this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.BADGE);
		if (badge.getItem() instanceof ItemBadge) {
			IItemHandler capability = badge.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
			for (int i = 0; i < capability.getSlots(); i++) {
				this.entityDropItem(capability.getStackInSlot(i), 0.0F);
			}
		}
		this.dropEquipment(wasRecentlyHit, lootingModifier);
	}

	@Override
	public void tick() {
		LivingEntity attackTarget = this.getAttackTarget();
		if (attackTarget != null) {
			if (this.isInSightRange(attackTarget) && this.getEntitySenses().canSee(attackTarget)) {
				this.lastTimeSeenAttackTarget = this.ticksExisted;
			}
			this.lastPosAttackTarget = attackTarget.getPositionVector();
		}

		super.tick();

		if (!this.world.isRemote && this.isMagicArmorActive()) {
			this.updateCooldownForMagicArmor();
		}
		if (!this.world.isRemote && !this.isNonBoss() && this.world.getDifficulty() == Difficulty.PEACEFUL) {
			SpawnerFactory.placeSpawner(new Entity[] { this }, false, null, this.world, this.getPosition());
			this.remove();
		}

		ItemStack stack = this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.POTION);
		if (!this.world.isRemote && stack != this.prevPotion) {
			CQRMain.NETWORK.sendToAll(new ItemStackSyncPacket(this.getEntityId(), EntityEquipmentExtraSlot.POTION.getIndex(), stack));
		}
		this.prevPotion = stack;

		if (this.isSneaking() && !this.prevSneaking) {
			this.resize(1.0F, 0.8F);
		} else if (!this.isSneaking() && this.prevSneaking) {
			this.resize(1.0F, 1.25F);
		}
		if (this.isSitting() && !this.prevSitting) {
			this.resize(1.0F, 0.75F);
		} else if (!this.isSitting() && this.prevSitting) {
			this.resize(1.0F, 4.0F / 3.0F);
		}
		this.prevSneaking = this.isSneaking();
		this.prevSitting = this.isSitting();

		if (!this.world.isRemote) {
			int spellInformation = 0;
			if (this.spellHandler != null) {
				if (this.spellHandler.isSpellCharging()) {
					spellInformation = spellInformation | 1 << 26;
				}
				if (this.spellHandler.isSpellCasting()) {
					spellInformation = spellInformation | 1 << 25;
				}
				if (this.spellHandler.getActiveSpell() instanceof IEntityAISpellAnimatedVanilla) {
					IEntityAISpellAnimatedVanilla spell = (IEntityAISpellAnimatedVanilla) this.spellHandler.getActiveSpell();
					spellInformation = spellInformation | 1 << 24;
					spellInformation = spellInformation | ((int) (spell.getRed() * 255.0D) & 255) << 16;
					spellInformation = spellInformation | ((int) (spell.getGreen() * 255.0D) & 255) << 8;
					spellInformation = spellInformation | (int) (spell.getBlue() * 255.0D) & 255;
				}
			}
			this.dataManager.set(SPELL_INFORMATION, spellInformation);
		} else {
			if (this.isSpellAnimated()) {
				int spellColor = this.dataManager.get(SPELL_INFORMATION);
				double red = (double) ((spellColor >> 16) & 255) / 255.0D;
				double green = (double) ((spellColor >> 8) & 255) / 255.0D;
				double blue = (double) (spellColor & 255) / 255.0D;
				float f = this.renderYawOffset * 0.017453292F + MathHelper.cos((float) this.ticksExisted * 0.6662F) * 0.25F;
				float f1 = MathHelper.cos(f);
				float f2 = MathHelper.sin(f);
				this.world.addParticle(ParticleTypes.EFFECT, this.getPosX() + (double) f1 * (double) this.getWidth(), this.getPosY() + (double) this.getHeight(), this.getPosZ() + (double) f2 * (double) this.getWidth(), red, green, blue);
				this.world.addParticle(ParticleTypes.EFFECT, this.getPosX() - (double) f1 * (double) this.getWidth(), this.getPosY() + (double) this.getHeight(), this.getPosZ() - (double) f2 * (double) this.getWidth(), red, green, blue);
			}
			if (this.isChatting() && this.ticksExisted % LayerCQRSpeechbubble.CHANGE_BUBBLE_INTERVAL == 0) {
				this.chooseNewRandomSpeechBubble();
			}
		}
	}

	@Override
	public SoundCategory getSoundCategory() {
		return SoundCategory.HOSTILE;
	}

	@Override
	public void livingTick() {
		this.updateArmSwingProgress();
		super.livingTick();
		
		if(!world.isRemote) {
			this.dataManager.set(HAS_TARGET, getAttackTarget() != null);
		}
	}

	@Override
	protected SoundEvent getSwimSound() {
		return SoundEvents.ENTITY_HOSTILE_SWIM;
	}

	@Override
	protected SoundEvent getSplashSound() {
		return SoundEvents.ENTITY_HOSTILE_SPLASH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return SoundEvents.ENTITY_HOSTILE_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.ENTITY_HOSTILE_DEATH;
	}

	@Override
	protected SoundEvent getFallSound(int heightIn) {
		return heightIn > 4 ? SoundEvents.ENTITY_HOSTILE_BIG_FALL : SoundEvents.ENTITY_HOSTILE_SMALL_FALL;
	}

	@Override
	public boolean attackEntityAsMob(Entity entityIn) {
		//Shoulder entity stuff
		spawnShoulderEntities();
		
		if (this.getHeldItemMainhand().getItem() instanceof ItemStaffHealing) {
			if (entityIn instanceof LivingEntity) {
				if (!this.world.isRemote) {
					((LivingEntity) entityIn).heal(ItemStaffHealing.HEAL_AMOUNT_ENTITIES);
					entityIn.setFire(0);
					((ServerWorld) this.world).spawnParticle(ParticleTypes.HEART, entityIn.getPosX(), entityIn.getPosY() + entityIn.getHeight() * 0.5D, entityIn.getPosZ(), 4, 0.25D, 0.25D, 0.25D, 0.0D);
					this.world.playSound(null, entityIn.getPosX(), entityIn.getPosY() + entityIn.getHeight() * 0.5D, entityIn.getPosZ(), ModSounds.MAGIC, SoundCategory.MASTER, 0.6F, 0.6F + this.rand.nextFloat() * 0.2F);
				}
				return true;
			}
			return false;
		}
		float f = (float) this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
		int i = 0;

		if (entityIn instanceof LivingEntity) {
			f += EnchantmentHelper.getModifierForCreature(this.getHeldItemMainhand(), ((LivingEntity) entityIn).getCreatureAttribute());
			i += EnchantmentHelper.getKnockbackModifier(this);
		}
		// Start IceAndFire compatibility
		if (CQRConfig.advanced.enableSpecialFeatures) {
			ResourceLocation resLoc = entityIn.getType().getRegistryName();//EntityList.getKey(entityIn);
			if (resLoc != null && resLoc.getNamespace().equalsIgnoreCase("iceandfire")) {
				f *= 2.0F;
			}
		}
		// End IceAndFire compatibility
		boolean flag = entityIn.attackEntityFrom(DamageSource.causeMobDamage(this), f);

		if (flag) {
			if (i > 0 && entityIn instanceof LivingEntity) {
				((LivingEntity) entityIn).knockBack(this, (float) i * 0.5F, (double) MathHelper.sin(this.rotationYaw * 0.017453292F), (double) (-MathHelper.cos(this.rotationYaw * 0.017453292F)));
				//this.motionX *= 0.6D;
				//this.motionZ *= 0.6D;
				this.setMotion(getMotion().x * 0.6, getMotion().y, getMotion().z * 0.6);
			}

			int j = EnchantmentHelper.getFireAspectModifier(this);

			if (j > 0) {
				entityIn.setFire(j * 4);
			}

			if (entityIn instanceof PlayerEntity) {
				PlayerEntity entityplayer = (PlayerEntity) entityIn;
				ItemStack itemstack = this.getHeldItemMainhand();
				ItemStack itemstack1 = entityplayer.isHandActive() ? entityplayer.getActiveItemStack() : ItemStack.EMPTY;

				if (!itemstack.isEmpty() && !itemstack1.isEmpty() && itemstack.getItem().canDisableShield(itemstack, itemstack1, entityplayer, this) && itemstack1.getItem().isShield(itemstack1, entityplayer)) {
					float f1 = 0.25F + (float) EnchantmentHelper.getEfficiencyModifier(this) * 0.05F;

					if (this.rand.nextFloat() < f1) {
						entityplayer.getCooldownTracker().setCooldown(itemstack1.getItem(), 100);
						this.world.setEntityState(entityplayer, (byte) 30);
					}
				}
			}

			this.applyEnchantments(this, entityIn);
		}
		
		return flag;
	}

	@Override
	protected boolean canDropLoot() {
		return true;
	}

	@Override
	public void writeSpawnData(PacketBuffer buffer) {
		buffer.writeFloat(this.getSizeVariation());
		buffer.writeDouble(this.getHealthScale());
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.HEAD));
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.CHEST));
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.LEGS));
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.FEET));
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.MAINHAND));
		buffer.writeFloat(this.getDropChance(EquipmentSlotType.OFFHAND));
		PacketBufferUtils.writeItemStack(buffer, this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.POTION));
	}

	@Override
	public void readSpawnData(PacketBuffer additionalData) {
		this.setSizeVariation(additionalData.readFloat());
		this.setHealthScale(additionalData.readDouble());
		this.setDropChance(EquipmentSlotType.HEAD, additionalData.readFloat());
		this.setDropChance(EquipmentSlotType.CHEST, additionalData.readFloat());
		this.setDropChance(EquipmentSlotType.LEGS, additionalData.readFloat());
		this.setDropChance(EquipmentSlotType.FEET, additionalData.readFloat());
		this.setDropChance(EquipmentSlotType.MAINHAND, additionalData.readFloat());
		this.setDropChance(EquipmentSlotType.OFFHAND, additionalData.readFloat());
		this.setItemStackToExtraSlot(EntityEquipmentExtraSlot.POTION, PacketBufferUtils.readItemStack(additionalData));
	}

	// Chocolate Quest Repoured
	public LivingEntity getLeader() {
		if (this.leaderUUID != null) {
			if (this.leader != null) {
				if (this.leader.isAlive()) {
					return this.leader;
				}
				this.leader = null;
				this.leaderUUID = null;
			} else if (this.world instanceof ServerWorld){
				/*for (Entity entity : this.world.loadedEntityList) {
					if (entity instanceof EntityLivingBase && this.leaderUUID.equals(entity.getPersistentID()) && entity.isEntityAlive()) {
						this.leader = (EntityLivingBase) entity;
						return (EntityLivingBase) entity;
					}
				}*/
				this.leader = (LivingEntity) ((ServerWorld) this.world).getEntityByUuid(this.leaderUUID);
			}
		} else {
			this.leader = null;
		}
		return null;
	}

	public void setLeader(LivingEntity leader) {
		if (leader != null && leader.isAlive()) {
			if (this.dimension == leader.dimension) {
				this.leader = leader;
			}
			this.leaderUUID = leader.getUniqueID();
		}
	}

	public boolean hasLeader() {
		return this.getLeader() != null;
	}

	public BlockPos getHomePositionCQR() {
		return this.homePosition;
	}

	public void setHomePositionCQR(BlockPos homePosition) {
		this.homePosition = homePosition;
	}

	public boolean hasHomePositionCQR() {
		return this.getHomePositionCQR() != null;
	}

	public abstract float getBaseHealth();

	public float calculateBaseHealth(double x, double z, float health) {
		BlockPos spawn = this.world.getSpawnPoint();
		x -= (double) spawn.getX();
		z -= (double) spawn.getZ();
		float distance = (float) Math.sqrt(x * x + z * z);

		health *= 1.0F + 0.1F * (int) (distance / CQRConfig.mobs.distanceDivisor);
		health *= this.healthScale;

		return (float) (int) health;
	}

	public void setBaseHealth(BlockPos pos, float health) {
		health = this.calculateBaseHealth(pos.getX(), pos.getZ(), health);
		this.getAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(health);
		this.setHealth(health);
	}

	public void handleArmorBreaking() {
		if (!this.world.isRemote && this.usedPotions + 1 > this.getHealingPotions()) {
			boolean armorBroke = false;
			float hpPrcntg = this.getHealth() / this.getMaxHealth();

			// below 80% health -> remove boobs
			if (hpPrcntg <= 0.8F) {
				if (!this.getItemStackFromSlot(EquipmentSlotType.FEET).isEmpty()) {
					this.setItemStackToSlot(EquipmentSlotType.FEET, ItemStack.EMPTY);
					armorBroke = true;
				}

				// below 60% health -> remove helmet
				if (hpPrcntg <= 0.6F) {
					if (!this.getItemStackFromSlot(EquipmentSlotType.HEAD).isEmpty()) {
						this.setItemStackToSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY);
						armorBroke = true;
					}

					// below 40% health -> remove leggings
					if (hpPrcntg <= 0.4F) {
						if (!this.getItemStackFromSlot(EquipmentSlotType.LEGS).isEmpty()) {
							this.setItemStackToSlot(EquipmentSlotType.LEGS, ItemStack.EMPTY);
							armorBroke = true;
						}

						// below 20% health -> remove chestplate
						if (hpPrcntg <= 0.2F) {
							if (!this.getItemStackFromSlot(EquipmentSlotType.CHEST).isEmpty()) {
								this.setItemStackToSlot(EquipmentSlotType.CHEST, ItemStack.EMPTY);
								armorBroke = true;
							}
						}
					}
				}
			}

			if (armorBroke) {
				this.playSound(SoundEvents.ENTITY_ITEM_BREAK, 1.75F, 0.8F);
			}
		}
	}

	public int getHealingPotions() {
		ItemStack stack = this.getHeldItemPotion();
		if (stack.getItem() instanceof ItemPotionHealing) {
			return stack.getCount();
		}
		return 0;
	}

	public void setHealingPotions(int amount) {
		ItemStack stack = new ItemStack(ModItems.POTION_HEALING, amount);
		if (this.holdingPotion) {
			this.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack);
		} else {
			this.setItemStackToExtraSlot(EntityEquipmentExtraSlot.POTION, stack);
		}
	}

	public ItemStack getItemStackFromExtraSlot(EntityEquipmentExtraSlot slot) {
		CapabilityExtraItemHandler capability = this.getCapability(CapabilityExtraItemHandlerProvider.EXTRA_ITEM_HANDLER, null);
		return capability.getStackInSlot(slot.getIndex());
	}

	public void setItemStackToExtraSlot(EntityEquipmentExtraSlot slot, ItemStack stack) {
		CapabilityExtraItemHandler capability = this.getCapability(CapabilityExtraItemHandlerProvider.EXTRA_ITEM_HANDLER, null);
		capability.setStackInSlot(slot.getIndex(), stack);
	}

	public void swapWeaponAndPotionSlotItemStacks() {
		ItemStack stack1 = this.getItemStackFromSlot(EquipmentSlotType.MAINHAND);
		ItemStack stack2 = this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.POTION);
		this.setItemStackToSlot(EquipmentSlotType.MAINHAND, stack2);
		this.setItemStackToExtraSlot(EntityEquipmentExtraSlot.POTION, stack1);
		this.holdingPotion = !this.holdingPotion;
	}

	public boolean isHoldingPotion() {
		return this.holdingPotion;
	}

	public abstract EDefaultFaction getDefaultFaction();

	public CQRFaction getDefaultFactionInstance() {
		if (this.defaultFactionInstance == null) {
			this.defaultFactionInstance = FactionRegistry.instance().getFactionInstance(this.getDefaultFaction().name());
		}
		return this.defaultFactionInstance;
	}

	@Nullable
	public CQRFaction getFaction() {
		if (this.hasLeader()) {
			return FactionRegistry.instance().getFactionOf(this.getLeader());
		}
		if (this.factionInstance == null && this.factionName != null && !this.factionName.isEmpty()) {
			this.factionInstance = FactionRegistry.instance().getFactionInstance(this.factionName);
		}
		if (this.factionInstance != null) {
			return this.factionInstance;
		}
		return this.getDefaultFactionInstance();
	}

	public void setFaction(String newFac) {
		this.factionInstance = null;
		this.factionName = newFac;
	}

	public boolean hasFaction() {
		return this.getFaction() != null;
	}

	public void updateReputationOnDeath(DamageSource cause) {
		if (cause.getTrueSource() instanceof PlayerEntity && this.hasFaction()) {
			PlayerEntity player = (PlayerEntity) cause.getTrueSource();
			int range = CQRConfig.mobs.factionUpdateRadius;
			double x1 = player.getPosX() - range;
			double y1 = player.getPosY() - range;
			double z1 = player.getPosZ() - range;
			double x2 = player.getPosX() + range;
			double y2 = player.getPosY() + range;
			double z2 = player.getPosZ() + range;
			AxisAlignedBB aabb = new AxisAlignedBB(x1, y1, z1, x2, y2, z2);

			List<CQRFaction> checkedFactions = new ArrayList<>();
			for (AbstractEntityCQR cqrentity : this.world.getEntitiesWithinAABB(AbstractEntityCQR.class, aabb)) {
				if (cqrentity.hasFaction() && !checkedFactions.contains(cqrentity.getFaction()) && (cqrentity.canEntityBeSeen(this) || cqrentity.canEntityBeSeen(player))) {
					CQRFaction faction = cqrentity.getFaction();
					if (this.getFaction().equals(faction)) {
						// DONE decrement the players repu on this entity's faction
						faction.decrementReputation(player, faction.getRepuMemberKill());
					} else if (this.getFaction().isEnemy(faction)) {
						// DONE increment the players repu at CQREntity's faction
						faction.incrementReputation(player, faction.getRepuEnemyKill());
					} else if (this.getFaction().isAlly(faction)) {
						// DONE decrement the players repu on CQREntity's faction
						faction.decrementReputation(player, faction.getRepuAllyKill());
					}
					checkedFactions.add(faction);
				}
			}
		}
	}

	public void onSpawnFromCQRSpawnerInDungeon(PlacementSettings placementSettings, EDungeonMobType mobType) {
		this.setHomePositionCQR(this.getPosition());
		this.setBaseHealth(this.getPosition(), this.getBaseHealth());

		// Recalculate path points
		if (this.pathPoints.length > 0) {
			for (int i = 0; i < this.pathPoints.length; i++) {
				this.pathPoints[i] = Template.transformedBlockPos(placementSettings, this.pathPoints[i]);
			}
		}

		// Replace shield
		for (EquipmentSlotType slot : EquipmentSlotType.values()) {
			ItemStack stack = this.getItemStackFromSlot(slot);
			Item item = stack.getItem();
			if (item instanceof ItemShieldDummy) {
				this.setItemStackToSlot(slot, mobType.getShieldItem().copy());
			}
		}
	}

	public boolean hasCape() {
		return false;
	}

	public ResourceLocation getResourceLocationOfCape() {
		return null;
	}

	public void setSizeVariation(float size) {
		this.resize(size / this.sizeScaling, size / this.sizeScaling);
		this.sizeScaling = size;
	}

	public float getSizeVariation() {
		return this.sizeScaling;
	}

	public void setSitting(boolean sitting) {
		this.dataManager.set(IS_SITTING, sitting);
	}

	public boolean isSitting() {
		return this.dataManager.get(IS_SITTING);
	}

	public void setChatting(boolean chatting) {
		this.dataManager.set(TALKING, chatting);
	}

	public boolean isChatting() {
		return this.dataManager.get(TALKING);
	}

	public void setArmPose(ECQREntityArmPoses pose) {
		this.dataManager.set(ARM_POSE, pose.toString());
	}

	public ECQREntityArmPoses getArmPose() {
		return ECQREntityArmPoses.valueOf(this.dataManager.get(ARM_POSE));
	}

	public boolean isLeader() {
		// TODO: Implement team building
		return false;
	}

	@OnlyIn(Dist.CLIENT)
	public ESpeechBubble getCurrentSpeechBubble() {
		return ESpeechBubble.values()[this.currentSpeechBubbleID];
	}

	@OnlyIn(Dist.CLIENT)
	public void chooseNewRandomSpeechBubble() {
		this.currentSpeechBubbleID = this.rand.nextInt(ESpeechBubble.values().length);
	}

	@OnlyIn(Dist.CLIENT)
	public int getTextureIndex() {
		return this.dataManager.get(TEXTURE_INDEX);
	}

	public int getTextureCount() {
		return 1;
	}

	public double getAttackReach(LivingEntity target) {
		return this.getWidth() + target.getWidth() + 0.25D;
	}

	public boolean isInAttackReach(LivingEntity target) {
		Vec3d vec1 = new Vec3d(this.getPosX(), MathHelper.clamp(target.getPosY(), this.getPosY(), this.getPosY() + this.getHeight()), this.getPosZ());
		Vec3d vec2 = new Vec3d(target.getPosX(), MathHelper.clamp(this.getPosY(), target.getPosY(), target.getPosY() + target.getHeight()), target.getPosZ());
		double d = this.getAttackReach(target);
		return vec1.squareDistanceTo(vec2) <= d * d;
	}

	public boolean canOpenDoors() {
		return true;
	}

	public boolean canPutOutFire() {
		return true;
	}

	public boolean canIgniteTorch() {
		return true;
	}

	public boolean canTameEntity() {
		return true;
	}

	public boolean canMountEntity() {
		return true;
	}

	public boolean isEntityInFieldOfView(LivingEntity target) {
		double x = target.getPosX() - this.getPosX();
		double z = target.getPosZ() - this.getPosZ();
		double d = Math.toDegrees(Math.atan2(-x, z));
		if (!ItemUtil.compareRotations(this.rotationYawHead, d, 80.0D)) {
			return false;
		}
		double y = target.getPosY() + target.getEyeHeight() - this.getPosY() - this.getEyeHeight();
		double xz = Math.sqrt(x * x + z * z);
		double d1 = Math.toDegrees(Math.atan2(y, xz));
		return ItemUtil.compareRotations(this.rotationPitch, d1, 50.0D);
	}

	public void setHealthScale(double healthScale) {
		this.healthScale = healthScale;
	}

	public double getHealthScale() {
		return this.healthScale;
	}

	public float getDropChance(EquipmentSlotType slot) {
		switch (slot.getSlotType()) {
		case HAND:
			return this.inventoryHandsDropChances[slot.getIndex()];
		case ARMOR:
			return this.inventoryArmorDropChances[slot.getIndex()];
		default:
			return 0.0F;
		}
	}

	public boolean isInSightRange(Entity target) {
		double sightRange = 32.0D;
		sightRange *= 0.6D + 0.4D * (double) this.world.getLight(new BlockPos(target)) / 15.0D;
		sightRange *= this.isPotionActive(Effects.BLINDNESS) ? 0.5D : 1.0D;
		return this.getDistance(target) <= sightRange;
	}

	public ItemStack getHeldItemWeapon() {
		return this.isHoldingPotion() ? this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.POTION) : this.getHeldItemMainhand();
	}

	public ItemStack getHeldItemPotion() {
		return this.isHoldingPotion() ? this.getHeldItemMainhand() : this.getItemStackFromExtraSlot(EntityEquipmentExtraSlot.POTION);
	}

	public boolean isMagicArmorActive() {
		if (!this.world.isRemote) {
			return this.armorActive;
		}
		return this.dataManager.get(MAGIC_ARMOR_ACTIVE);
	}

	public void setMagicArmorActive(boolean val) {
		if (val != this.armorActive) {
			this.armorActive = val;
			this.setInvulnerable(this.armorActive);
			this.dataManager.set(MAGIC_ARMOR_ACTIVE, val);
		}
	}

	protected void updateCooldownForMagicArmor() {
		this.magicArmorCooldown--;
		if (this.magicArmorCooldown <= 0) {
			this.setMagicArmorActive(false);
		}
	}

	public void setMagicArmorCooldown(int val) {
		this.magicArmorCooldown = val;
		this.setMagicArmorActive(true);
	}

	public float getDefaultWidth() {
		return 0.6F;
	}

	public float getDefaultHeight() {
		return 1.95F;
	}

	public void resize(float widthScale, float heightSacle) {
		this.setSize(this.getWidth() * widthScale, this.getHeight() * heightSacle);
		if (this.stepHeight * heightSacle >= 1.0) {
			this.stepHeight *= heightSacle;
		}
	}

	public BlockPos[] getGuardPathPoints() {
		return this.pathPoints;
	}

	public boolean isGuardPathLoop() {
		return this.pathIsLoop;
	}

	public int getCurrentGuardPathTargetPoint() {
		return this.currentTargetPoint;
	}

	public void setCurrentGuardPathTargetPoint(int value) {
		this.currentTargetPoint = value;
	}

	public void addPathPoint(BlockPos position) {
		if (this.getHomePositionCQR() == null) {
			this.setHomePositionCQR(position);
		}
		BlockPos[] newPosArr = new BlockPos[this.pathPoints.length + 1];
		for (int i = 0; i < this.pathPoints.length; i++) {
			newPosArr[i] = this.pathPoints[i];
		}
		position = position.subtract(this.getHomePositionCQR());
		newPosArr[this.pathPoints.length] = position;
		this.pathPoints = newPosArr;
	}

	public void clearPathPoints() {
		this.pathPoints = new BlockPos[] {};
	}

	public void setPath(final BlockPos[] path) {
		if (path.length <= 0) {
			this.pathPoints = new BlockPos[] {};
		}
		this.pathPoints = new BlockPos[path.length];
		for (int i = 0; i < path.length; i++) {
			if (this.getHomePositionCQR() == null) {
				this.setHomePositionCQR(path[i]);
			}
			this.pathPoints[i] = path[i].subtract(this.getHomePositionCQR());
		}
	}

	public int getLastTimeSeenAttackTarget() {
		return this.lastTimeSeenAttackTarget;
	}

	public Vec3d getLastPosAttackTarget() {
		return this.lastPosAttackTarget;
	}

	public EntityAISpellHandler createSpellHandler() {
		return new EntityAISpellHandler(this, 200);
	}

	public boolean isSpellCharging() {
		return (this.dataManager.get(SPELL_INFORMATION) >> 26 & 1) == 1;
	}

	public boolean isSpellCasting() {
		return (this.dataManager.get(SPELL_INFORMATION) >> 25 & 1) == 1;
	}

	public boolean isSpellAnimated() {
		return (this.dataManager.get(SPELL_INFORMATION) >> 24 & 1) == 1;
	}

	public void setLastTimeHitByAxeWhileBlocking(int tick) {
		this.lastTimeHitByAxeWhileBlocking = tick;
	}

	public int getLastTimeHitByAxeWhileBlocking() {
		return this.lastTimeHitByAxeWhileBlocking;
	}
	
	@SideOnly(Side.CLIENT)
	public boolean hasAttackTarget() {
		return this.dataManager.get(HAS_TARGET);
	}
	
	
	
	
	
	//Shoulder entity stuff

	public boolean addShoulderEntity(CompoundNBT p_192027_1_)
    {
        if (!this.isRiding() && this.onGround && !this.isInWater())
        {
            if (this.getLeftShoulderEntity().isEmpty())
            {
                this.setLeftShoulderEntity(p_192027_1_);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }

    protected void spawnShoulderEntities()
    {
        this.spawnShoulderEntity(this.getLeftShoulderEntity());
        this.setLeftShoulderEntity(new CompoundNBT());
    }

    private void spawnShoulderEntity(@Nullable CompoundNBT p_192026_1_)
    {
        if (!this.world.isRemote && !p_192026_1_.isEmpty())
        {
        	//TODO
            Entity entity = EntityList.createEntityFromNBT(p_192026_1_, this.world);

            if (entity instanceof EntityTameable)
            {
                ((EntityTameable)entity).setOwnerId(this.entityUniqueID);
            }

            entity.setPosition(this.posX, this.posY + 0.699999988079071D, this.posZ);
            this.world.spawnEntity(entity);
        }
    }
    
    public CompoundNBT getLeftShoulderEntity()
    {
        return (CompoundNBT)this.dataManager.get(SHOULDER_ENTITY);
    }

    protected void setLeftShoulderEntity(CompoundNBT tag)
    {
        this.dataManager.set(SHOULDER_ENTITY, tag);
    }
    
    protected void setSize(float w, float h) {
    	//TODO
    }
    
    protected boolean isRiding() {
    	return this.getRidingEntity() != null;
    }

}
