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

package me.fallenbreath.tweakermore.mixins.tweaks.mc_tweaks.disableEntityRenderInterpolation;

import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

//#if MC >= 11500
import org.spongepowered.asm.mixin.injection.ModifyArg;
//#else
//$$ import org.spongepowered.asm.mixin.injection.ModifyVariable;
//#endif

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin
{
	//#if MC >= 11500
	@ModifyArg(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/WorldRenderer;renderEntity(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V"
			)
	)
	//#else
	//$$ @ModifyVariable(method = "renderEntities", at = @At("HEAD"), argsOnly = true)
	//#endif
	private float disableEntityRenderInterpolation(float tickDelta)
	{
		if (TweakerMoreConfigs.DISABLE_ENTITY_RENDER_INTERPOLATION.getBooleanValue())
		{
			tickDelta = 1.0F;
		}
		return tickDelta;
	}
}
