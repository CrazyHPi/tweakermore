/*
 * This file is part of the TweakerMore project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2023  Fallen_Breath and contributors
 *
 * TweakerMore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TweakerMore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TweakerMore.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.fallenbreath.tweakermore.mixins.tweaks.features.safeAfk;

import me.fallenbreath.tweakermore.impl.features.safeAfk.SafeAfkHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC >= 12002
//$$ import net.minecraft.client.network.ClientCommonNetworkHandler;
//$$ import net.minecraft.client.network.ClientConnectionState;
//$$ import net.minecraft.network.ClientConnection;
//#else
import org.spongepowered.asm.mixin.Shadow;
//#endif

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin
		//#if MC >= 12002
		//$$ extends ClientCommonNetworkHandler
		//#endif
{
	//#if MC >= 12002
	//$$ protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState)
	//$$ {
	//$$ 	super(client, connection, connectionState);
	//$$ }
	//#else
	@Shadow private MinecraftClient client;
	//#endif

	@Inject(method = "onHealthUpdate", at = @At("TAIL"))
	private void tweakerMoreSafeAfkHook(CallbackInfo ci)
	{
		SafeAfkHelper.onHealthUpdate(this.client);
	}

	@Inject(method = {"clearWorld", "onPlayerRespawn"}, at = @At("TAIL"))
	private void resetLastHurtGameTime(CallbackInfo ci)
	{
		SafeAfkHelper.resetHurtTime();
	}
}
