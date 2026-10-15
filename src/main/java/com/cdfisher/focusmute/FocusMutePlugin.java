package com.cdfisher.focusmute;

import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@PluginDescriptor(
	name = "Focus Mute"
)
public class FocusMutePlugin extends Plugin
{
	static final String CONFIG_GROUP = "focusmute";

	private boolean focused;
	private boolean enabled;

	// Null means this plugin has not taken ownership of the channel's volume.
	private Integer musicVolume;
	private Integer soundEffectVolume;
	private Integer areaSoundEffectVolume;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private FocusMuteConfig config;

	@Override
	protected void startUp()
	{
		clientThread.invoke(() ->
		{
			enabled = true;
			focused = client.getCanvas().isFocusOwner();
			refreshMutes();
		});
	}

	@Override
	protected void shutDown()
	{
		clientThread.invoke(() ->
		{
			enabled = false;
			restoreVolumes();
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (CONFIG_GROUP.equals(event.getGroup()))
		{
			clientThread.invoke(this::refreshMutes);
		}
	}

	@Subscribe
	public void onFocusChanged(FocusChanged event)
	{
		clientThread.invoke(() ->
		{
			focused = event.isFocused();
			refreshMutes();
		});
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed event)
	{
		if (enabled && !focused && config.muteSoundEffects())
		{
			event.consume();
		}
	}

	@Subscribe
	public void onAreaSoundEffectPlayed(AreaSoundEffectPlayed event)
	{
		// Suppress positional sounds (including our own and other players' mining)
		// at playback time as well as setting the area's volume preference.
		if (enabled && !focused && config.muteAreaSounds())
		{
			event.consume();
		}
	}

	private void refreshMutes()
	{
		if (!enabled)
		{
			return;
		}
		if (focused)
		{
			restoreVolumes();
			return;
		}

		if (config.muteMusic())
		{
			if (musicVolume == null)
			{
				musicVolume = client.getMusicVolume();
			}
			client.setMusicVolume(0);
		}
		else if (musicVolume != null)
		{
			client.setMusicVolume(musicVolume);
			musicVolume = null;
		}

		if (config.muteSoundEffects())
		{
			if (soundEffectVolume == null)
			{
				soundEffectVolume = client.getPreferences().getSoundEffectVolume();
			}
			client.getPreferences().setSoundEffectVolume(0);
		}
		else if (soundEffectVolume != null)
		{
			client.getPreferences().setSoundEffectVolume(soundEffectVolume);
			soundEffectVolume = null;
		}

		if (config.muteAreaSounds())
		{
			if (areaSoundEffectVolume == null)
			{
				areaSoundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
			}
			client.getPreferences().setAreaSoundEffectVolume(0);
		}
		else if (areaSoundEffectVolume != null)
		{
			client.getPreferences().setAreaSoundEffectVolume(areaSoundEffectVolume);
			areaSoundEffectVolume = null;
		}
	}

	private void restoreVolumes()
	{
		if (musicVolume != null)
		{
			client.setMusicVolume(musicVolume);
			musicVolume = null;
		}
		if (soundEffectVolume != null)
		{
			client.getPreferences().setSoundEffectVolume(soundEffectVolume);
			soundEffectVolume = null;
		}
		if (areaSoundEffectVolume != null)
		{
			client.getPreferences().setAreaSoundEffectVolume(areaSoundEffectVolume);
			areaSoundEffectVolume = null;
		}
	}

	@Provides
	FocusMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FocusMuteConfig.class);
	}
}
