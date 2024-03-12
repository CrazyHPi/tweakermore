/*
 * This file is part of the TweakerMore project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2024  Fallen_Breath and contributors
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

package me.fallenbreath.tweakermore.mixins.tweaks.features.fireworkRocketThrottler;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import fi.dy.masa.malilib.util.InfoUtils;
import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FireworkItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin
{
	@Unique
	private long lastFireworkRocketUsageMilli = 0;

	// ========================== activate cooldown ==========================

	@Inject(
			method = "interactBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V",
					ordinal = 2
			),
			cancellable = true
	)
	private void fireworkRocketThrottler_cancelIfCooldown_useOnBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir)
	{
		cancelIfCooldown(player, hand, cir);
	}

	@Inject(
			method = "interactItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"
			),
			cancellable = true
	)
	private void fireworkRocketThrottler_cancelIfCooldown_useAtAir(PlayerEntity player, World world, Hand hand, CallbackInfoReturnable<ActionResult> cir)
	{
		cancelIfCooldown(player, hand, cir);
	}

	@Unique
	private void cancelIfCooldown(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir)
	{
		if (TweakerMoreConfigs.FIREWORK_ROCKET_THROTTLER.getBooleanValue())
		{
			ItemStack itemStack = player.getStackInHand(hand);
			if (itemStack.getItem() instanceof FireworkItem)
			{
				long now = System.currentTimeMillis();
				double cooldown = TweakerMoreConfigs.FIREWORK_ROCKET_THROTTLER_COOLDOWN.getDoubleValue();
				double remaining = cooldown - (now - this.lastFireworkRocketUsageMilli) / 1000.0;
				if (remaining > 0)
				{
					InfoUtils.printActionbarMessage("tweakermore.impl.fireworkRocketThrottler.throttled", String.format("%.1f", remaining));
					cir.setReturnValue(ActionResult.FAIL);
				}
			}
		}
	}

	// ========================== update cooldown ==========================

	@ModifyExpressionValue(
			method = "interactBlock",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"
			)
	)
	private ActionResult fireworkRocketThrottler_updateCooldown_useOnBlock(ActionResult actionResult)
	{
		updateCooldownOnUse(actionResult);
		return actionResult;
	}

	@ModifyExpressionValue(
			method = "interactItem",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/TypedActionResult;getResult()Lnet/minecraft/util/ActionResult;"
			)
	)
	private ActionResult fireworkRocketThrottler_updateCooldown_useAtAir(ActionResult actionResult)
	{
		updateCooldownOnUse(actionResult);
		return actionResult;
	}

	@Unique
	private void updateCooldownOnUse(ActionResult actionResult)
	{
		if (TweakerMoreConfigs.FIREWORK_ROCKET_THROTTLER.getBooleanValue())
		{
			if (
					//#if MC >= 11500
					actionResult.isAccepted()
					//#else
					//$$ actionResult == ActionResult.SUCCESS
					//#endif
			)
			{
				this.lastFireworkRocketUsageMilli = System.currentTimeMillis();
			}
		}
	}
}
