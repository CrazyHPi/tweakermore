package me.fallenbreath.tweakermore.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.GuiConfigsBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.widgets.WidgetLabel;
import fi.dy.masa.malilib.hotkeys.IKeybind;
import fi.dy.masa.malilib.hotkeys.KeyAction;
import fi.dy.masa.malilib.util.StringUtils;
import me.fallenbreath.tweakermore.TweakerMoreMod;
import me.fallenbreath.tweakermore.config.Config;
import me.fallenbreath.tweakermore.config.TweakerMoreConfigs;
import me.fallenbreath.tweakermore.config.TweakerMoreOption;
import me.fallenbreath.tweakermore.util.FabricUtil;
import me.fallenbreath.tweakermore.util.JsonSaveAble;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

//#if MC >= 11600
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

import java.util.*;
import java.util.stream.Collectors;

public class TweakerMoreConfigGui extends GuiConfigsBase
{
	@Nullable
	private static TweakerMoreConfigGui currentInstance = null;
	@Nullable
	private Config.Type filteredType = null;
	@Nullable
	private SelectorDropDownList<Config.Type> typeFilterDropDownList = null;

	private static final Setting SETTING = new Setting();

	public TweakerMoreConfigGui()
	{
		super(10, 50, TweakerMoreMod.MOD_ID, null, "tweakermore.gui.title", TweakerMoreMod.VERSION);
		currentInstance = this;
	}

	@Override
	public void removed()
	{
		super.removed();
		currentInstance = null;
	}

	public static Setting getSetting()
	{
		return SETTING;
	}

	public static Optional<TweakerMoreConfigGui> getCurrentInstance()
	{
		return Optional.ofNullable(currentInstance);
	}

	public static boolean onOpenGuiHotkey(KeyAction keyAction, IKeybind iKeybind)
	{
		GuiBase.openGui(new TweakerMoreConfigGui());
		return true;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.clearOptions();

		int x = 10;
		int y = 26;

		for (Config.Category category : Config.Category.values())
		{
			x += this.createNavigationButton(x, y, category);
		}

		Set<Config.Type> possibleTypes = TweakerMoreConfigs.getOptions(SETTING.category).stream().map(TweakerMoreOption::getType).collect(Collectors.toSet());
		List<Config.Type> items = Arrays.stream(Config.Type.values()).filter(possibleTypes::contains).collect(Collectors.toList());
		items.add(0, null);
		SelectorDropDownList<Config.Type> dd = new SelectorDropDownList<>(this.width - 91, this.getListY() + 3, 80, 16, 200, items.size(), items);
		dd.setEntryChangeListener(type -> {
			if (type != this.filteredType)
			{
				this.filteredType = type;
				this.reDraw();
			}
		});
		this.addWidget(dd);
		dd.setNullEntry(() -> StringUtils.translate("tweakermore.gui.selector_drop_down_list.all"));
		dd.setHoverText("tweakermore.gui.config_type.label_text");
		this.typeFilterDropDownList = dd;
		dd.setSelectedEntry(this.filteredType);
	}

	private int createNavigationButton(int x, int y, Config.Category category)
	{
		ButtonGeneric button = new ButtonGeneric(x, y, -1, 20, category.getDisplayName());
		button.setEnabled(SETTING.category != category);
		button.setHoverStrings(category.getDescription());
		this.addButton(button, (b, mouseButton) -> {
			SETTING.category = category;
			this.reDraw();
		});
		return button.getWidth() + 2;
	}

	public void reDraw()
	{
		this.reCreateListWidget(); // apply the new config width
		Objects.requireNonNull(this.getListWidget()).resetScrollbarPosition();
		this.initGui();
	}

	public void renderDropDownList(
			//#if MC >= 11600
			//$$ MatrixStack matrixStack,
			//#endif
			int mouseX, int mouseY
	)
	{
		if (this.typeFilterDropDownList != null)
		{
			this.typeFilterDropDownList.render(
					mouseX, mouseY, this.typeFilterDropDownList.isMouseOver(mouseX, mouseY)
					//#if MC >= 11600
					//$$ , matrixStack
					//#endif
			);
		}
	}

	public Pair<Integer, Integer> adjustWidths(int guiWidth, int maxTextWidth)
	{
		int labelWidth;
		int panelWidth = 190;

		//#if MC >= 11800
		//$$ guiWidth -= 74;
		//#else
		guiWidth -= 75;
		//#endif

		// tweak label width first, to make sure the panel is not too close or too far from the label
		labelWidth = MathHelper.clamp(guiWidth - panelWidth, maxTextWidth - 5, maxTextWidth + 100);
		// decrease the panel width if space is not enough
		panelWidth = MathHelper.clamp(guiWidth - labelWidth, 100, panelWidth);
		// decrease the label width for a bit if space is still way not enough (the label text might overlap with the panel now)
		labelWidth = MathHelper.clamp(guiWidth - panelWidth + 25, labelWidth - Math.max((int)(maxTextWidth * 0.4), 30), labelWidth);

		// just in case
		labelWidth = Math.max(labelWidth, 0);
		panelWidth = Math.max(panelWidth, 0);

		return Pair.of(labelWidth, panelWidth);
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs()
	{
		List<IConfigBase> configs = Lists.newArrayList();
		for (TweakerMoreOption tweakerMoreOption : TweakerMoreConfigs.getOptions(SETTING.category))
		{
			// drop down list filtering logic
			if (this.filteredType != null && tweakerMoreOption.getType() != this.filteredType)
			{
				continue;
			}
			// hide disable options if config hideDisabledOptions is enabled
			if (TweakerMoreConfigs.HIDE_DISABLE_OPTIONS.getBooleanValue() && !tweakerMoreOption.isEnabled())
			{
				continue;
			}
			// hide options that don't work with current Minecraft versions, unless debug mode on
			if (!tweakerMoreOption.worksForCurrentMCVersion() && !TweakerMoreConfigs.TWEAKERMORE_DEBUG_MODE.getBooleanValue())
			{
				continue;
			}
			// hide debug options unless debug mode on
			if (tweakerMoreOption.isDebug() && !TweakerMoreConfigs.TWEAKERMORE_DEBUG_MODE.getBooleanValue())
			{
				continue;
			}
			// hide dev only options unless debug mode on and is dev env
			if (tweakerMoreOption.isDevOnly() && !(TweakerMoreConfigs.TWEAKERMORE_DEBUG_MODE.getBooleanValue() && FabricUtil.isDevelopmentEnvironment()))
			{
				continue;
			}
			configs.add(tweakerMoreOption.getConfig());
		}
		configs.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
		return ConfigOptionWrapper.createFor(configs);
	}

	private static class Setting implements JsonSaveAble
	{
		public Config.Category category = Config.Category.FEATURES;

		@Override
		public void dumpToJson(JsonObject jsonObject)
		{
			jsonObject.addProperty("category", this.category.name());
		}

		@Override
		public void loadFromJson(JsonObject jsonObject)
		{
			this.category = Config.Category.valueOf(jsonObject.get("category").getAsString());
		}
	}
}
