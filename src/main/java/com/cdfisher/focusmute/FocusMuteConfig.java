package com.cdfisher.focusmute;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(FocusMutePlugin.CONFIG_GROUP)
public interface FocusMuteConfig extends Config
{
	@ConfigSection(
		name = "Sound options",
		description = "Options for which types of sounds to mute",
		position = 0
	)
	String soundOptionsSection = "soundOptionsSection";

	@ConfigItem(
		keyName = "muteMusic",
		name = "Mute music",
		description = "Mute game music on focus change",
		position = 1,
		section = soundOptionsSection
	)

	default boolean muteMusic()
	{
		return true;
	}

	@ConfigItem(
		keyName = "muteSoundEffects",
		name = "Mute sound effects",
		description = "Mute sound effects on focus change",
		position = 2,
		section = soundOptionsSection
	)

	default boolean muteSoundEffects()
	{
		return true;
	}

	@ConfigItem(
		keyName = "muteAreaSounds",
		name = "Mute area sounds",
		description = "Mute area sounds on focus change",
		position = 3,
		section = soundOptionsSection
	)

	default boolean muteAreaSounds()
	{
		return true;
	}
}
