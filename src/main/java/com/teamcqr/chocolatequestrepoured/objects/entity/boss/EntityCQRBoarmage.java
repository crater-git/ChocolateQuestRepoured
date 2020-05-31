package com.teamcqr.chocolatequestrepoured.objects.entity.boss;

import java.util.ArrayList;
import java.util.List;

import com.teamcqr.chocolatequestrepoured.factions.CQRFaction;
import com.teamcqr.chocolatequestrepoured.factions.EDefaultFaction;
import com.teamcqr.chocolatequestrepoured.init.ModLoottables;
import com.teamcqr.chocolatequestrepoured.objects.entity.EBaseHealths;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.EntityAIExplosionRay;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.EntityAIExplosionSpell;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.EntityAISummonFireWall;
import com.teamcqr.chocolatequestrepoured.objects.entity.ai.spells.EntityAISummonMeteors;
import com.teamcqr.chocolatequestrepoured.objects.entity.bases.ISummoner;

import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class EntityCQRBoarmage extends AbstractEntityCQRMageBase implements ISummoner {

	protected List<Entity> summonedMinions = new ArrayList<>();

	public EntityCQRBoarmage(World worldIn, EntityType<? extends EntityCQRBoarmage> type) {
		super(worldIn, type);

		this.isImmuneToFire = true;
	}
	
	@Override
	public boolean isImmuneToExplosions() {
		return true;
	}

	@Override
	public void livingTick() {
		super.livingTick();
		List<Entity> tmp = new ArrayList<>();
		for (Entity ent : this.summonedMinions) {
			if (ent == null || !ent.isAlive()) {
				tmp.add(ent);
			}
		}
		for (Entity e : tmp) {
			this.summonedMinions.remove(e);
		}
	}

	@Override
	public void onDeath(DamageSource cause) {
		// Kill minions
		for (Entity e : this.summonedMinions) {
			if (e != null && e.isAlive()) {
				if (e instanceof LivingEntity) {
					((LivingEntity) e).onDeath(cause);
				}
				if (e != null) {
					e.remove();
				}
			}
		}
		this.summonedMinions.clear();

		super.onDeath(cause);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.spellHandler.addSpell(0, new EntityAISummonMeteors(this, 400, 40));
		this.spellHandler.addSpell(1, new EntityAIExplosionRay(this, 400, 40));
		this.spellHandler.addSpell(2, new EntityAIExplosionSpell(this, 400, 40));
		this.spellHandler.addSpell(3, new EntityAISummonFireWall(this, 400, 40));
	}

	@Override
	protected ResourceLocation getLootTable() {
		return ModLoottables.ENTITIES_BOARMAGE;
	}

	@Override
	public float getBaseHealth() {
		return EBaseHealths.BOAR_MAGE.getValue();
	}

	@Override
	public EDefaultFaction getDefaultFaction() {
		return EDefaultFaction.UNDEAD;
	}

	@Override
	public CQRFaction getSummonerFaction() {
		return this.getFaction();
	}

	@Override
	public List<Entity> getSummonedEntities() {
		return this.summonedMinions;
	}

	@Override
	public LivingEntity getSummoner() {
		return this;
	}

	@Override
	public void addSummonedEntityToList(Entity summoned) {
		this.summonedMinions.add(summoned);
	}

	@Override
	public CreatureAttribute getCreatureAttribute() {
		return CreatureAttribute.UNDEAD;
	}

	@Override
	public boolean canPutOutFire() {
		return false;
	}

}
