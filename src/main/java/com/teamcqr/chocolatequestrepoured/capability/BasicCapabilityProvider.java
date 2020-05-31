package com.teamcqr.chocolatequestrepoured.capability;

import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

public class BasicCapabilityProvider<C> implements ICapabilityProvider {

	public final Capability<C> capability;
	public final C instance;

	public BasicCapabilityProvider(Capability<C> capability, C instance) {
		this.capability = capability;
		this.instance = instance;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, Direction facing) {
		return capability == this.capability;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, Direction facing) {
		return capability == this.capability ? this.capability.<T>cast(this.instance) : null;
	}

}
