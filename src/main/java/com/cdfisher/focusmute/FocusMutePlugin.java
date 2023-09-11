package com.cdfisher.focusmute;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.api.events.FocusChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ChatMessage;


@Slf4j
@PluginDescriptor(
	name = "Focus Mute"
)
public class FocusMutePlugin extends Plugin
{
	static final String CONFIG_GROUP = "focusmute";

	boolean muteMusic;
	boolean muteSoundEffects;
	boolean muteAreaSounds;

	private int musicVolume;
	private int soundEffectVolume;
	private int areaSoundEffectVolume;

	private int initialMusicVolume;
	private int initialSoundEffectVolume;
	private int initialAreaSoundEffectVolume;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private FocusMuteConfig config;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Focus mute started!");
		loadOptions();
		// Duplicated from onChatMessage so the game doesn't get muted if it's turned on after logging in
		clientThread.invoke(() ->
		{
			initialMusicVolume = client.getMusicVolume();
			initialSoundEffectVolume = client.getPreferences().getSoundEffectVolume();
			initialAreaSoundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
		});
	}

	@Override
	protected void shutDown() throws Exception
	{
		// unmute on shutdown
		clientThread.invoke(() ->
		{
			client.setMusicVolume(initialMusicVolume);
			client.getPreferences().setSoundEffectVolume(initialSoundEffectVolume);
			client.getPreferences().setAreaSoundEffectVolume(initialAreaSoundEffectVolume);
		});
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (!CONFIG_GROUP.equals(event.getGroup()))
		{
			return;
		}

		loadOptions();
		refreshMutes();

/*		//unmute anything that may have been muted
		if (!muteMusic)
		{
			clientThread.invoke(() ->
			{
				client.setMusicVolume(initialMusicVolume);
			});
		}
		if (!muteSoundEffects)
		{
			clientThread.invoke(() ->
			{
				client.getPreferences().setSoundEffectVolume(initialSoundEffectVolume);
			});
		}
		if (!muteAreaSounds)
		{
			clientThread.invoke(() ->
			{
				client.getPreferences().setAreaSoundEffectVolume(initialAreaSoundEffectVolume);
			});
		}*/
	}



/*	@Subscribe
	// Saves initial volume values after player has logged in so they aren't just getting set to zero
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState().equals(GameState.LOGGED_IN))
		{
			clientThread.invoke(() ->
			{
				initialMusicVolume = client.getMusicVolume();
				initialSoundEffectVolume = client.getPreferences().getSoundEffectVolume();
				initialAreaSoundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
			});
			log.info(String.format("Initial volume values: Music: %d Sounds: %d Area sounds: %d", initialMusicVolume,
				initialSoundEffectVolume, initialAreaSoundEffectVolume));
		}
	}*/

	@Subscribe
	// Save initial volume values upon getting the "Welcome to RuneScape" chat message"
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (!chatMessage.getType().equals(ChatMessageType.WELCOME)) {
			return;
		}
		clientThread.invoke(() ->
		{
			initialMusicVolume = client.getMusicVolume();
			initialSoundEffectVolume = client.getPreferences().getSoundEffectVolume();
			initialAreaSoundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
		});
	}

	private void loadOptions()
	{
		muteMusic = config.muteMusic();
		muteSoundEffects = config.muteSoundEffects();
		muteAreaSounds = config.muteAreaSounds();
	}

	private void refreshMutes()
	{
		if (!muteMusic)
		{
			clientThread.invoke(() -> client.setMusicVolume(initialMusicVolume));
		}
		else
		{
			clientThread.invoke(() -> {
				musicVolume = client.getMusicVolume();
				client.setMusicVolume(0);
			});
		}
		if (!muteSoundEffects)
		{
			client.getPreferences().setSoundEffectVolume(initialSoundEffectVolume);
		}
		else
		{
			clientThread.invoke(() -> {
				soundEffectVolume = client.getPreferences().getSoundEffectVolume();
				client.getPreferences().setSoundEffectVolume(0);
			});
		}
		if (!muteAreaSounds)
		{
			client.getPreferences().setAreaSoundEffectVolume(initialAreaSoundEffectVolume);
		}
		else
		{
			clientThread.invoke(() -> {
				soundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
				client.getPreferences().setAreaSoundEffectVolume(0);
			});
		}
	}

	@Subscribe
	public void onFocusChanged(FocusChanged focusChanged)
	{
		if (focusChanged.isFocused())
		{
			// unmute
			clientThread.invoke(() ->
			{
				if (muteMusic)
				{
					client.setMusicVolume(musicVolume);
				}
				if (muteSoundEffects)
				{
					client.getPreferences().setSoundEffectVolume(soundEffectVolume);
				}
				if (muteAreaSounds)
				{
					client.getPreferences().setAreaSoundEffectVolume(areaSoundEffectVolume);
				}

			});
		}
		else
		{
			// mute
			clientThread.invoke(() ->
			{
				if (muteMusic)
				{
					musicVolume = client.getMusicVolume();
					client.setMusicVolume(0);
				}
				if (muteSoundEffects)
				{
					soundEffectVolume = client.getPreferences().getSoundEffectVolume();
					client.getPreferences().setSoundEffectVolume(0);
				}
				if (muteAreaSounds)
				{
					areaSoundEffectVolume = client.getPreferences().getAreaSoundEffectVolume();
					client.getPreferences().setAreaSoundEffectVolume(0);
				}
			});
		}
	}

	@Provides
	FocusMuteConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FocusMuteConfig.class);
	}
}
