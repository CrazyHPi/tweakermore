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

package me.fallenbreath.tweakermore.impl.features.autoContainerProcess.processors;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import me.fallenbreath.tweakermore.mixins.tweaks.features.autoContainerProcess.EnderChestBlockAccessor;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.container.ContainerType;
import net.minecraft.text.Text;

class ContainerProcessorUtils
{
	@SuppressWarnings("RedundantIfStatement")
	public static boolean shouldSkipForEnderChest(ContainerScreen<?> containerScreen, ConfigBoolean ignoreSwitch)
	{
		if (ignoreSwitch.getBooleanValue())
		{
			Text enderChestTitle = EnderChestBlockAccessor.getContainerName();
			// 9x3 container + title is "container.enderchest" == ender chest
			if (containerScreen.getContainer().getType() == ContainerType.GENERIC_9X3 && enderChestTitle.equals(containerScreen.getTitle()))
			{
				return true;
			}
		}
		return false;
	}
}
