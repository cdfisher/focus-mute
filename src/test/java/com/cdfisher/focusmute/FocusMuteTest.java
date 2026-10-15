package com.cdfisher.focusmute;

import java.awt.Canvas;
import java.lang.reflect.Field;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.Preferences;
import net.runelite.api.events.AreaSoundEffectPlayed;
import net.runelite.api.events.FocusChanged;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.events.ConfigChanged;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FocusMuteTest
{
	private final Client client = mock(Client.class);
	private final Preferences preferences = mock(Preferences.class);
	private final Canvas canvas = mock(Canvas.class);
	private final ClientThread clientThread = mock(ClientThread.class);
	private final FocusMuteConfig config = mock(FocusMuteConfig.class);
	private final FocusMutePlugin plugin = new FocusMutePlugin();
	private int music = 120;
	private int sound = 80;
	private int area = 50;

	@Before
	public void setUp() throws Exception
	{
		inject("client", client);
		inject("clientThread", clientThread);
		inject("config", config);
		when(client.getCanvas()).thenReturn(canvas);
		when(canvas.isFocusOwner()).thenReturn(true);
		when(client.getPreferences()).thenReturn(preferences);
		when(config.muteMusic()).thenReturn(true);
		when(config.muteSoundEffects()).thenReturn(true);
		when(config.muteAreaSounds()).thenReturn(true);
		doAnswer(call -> { call.getArgument(0, Runnable.class).run(); return null; })
			.when(clientThread).invoke(any(Runnable.class));
		when(client.getMusicVolume()).thenAnswer(call -> music);
		when(preferences.getSoundEffectVolume()).thenAnswer(call -> sound);
		when(preferences.getAreaSoundEffectVolume()).thenAnswer(call -> area);
		doAnswer(call -> { music = call.getArgument(0); return null; }).when(client).setMusicVolume(anyInt());
		doAnswer(call -> { sound = call.getArgument(0); return null; }).when(preferences).setSoundEffectVolume(anyInt());
		doAnswer(call -> { area = call.getArgument(0); return null; }).when(preferences).setAreaSoundEffectVolume(anyInt());
		plugin.startUp();
	}

	private void inject(String name, Object value) throws Exception
	{
		Field field = FocusMutePlugin.class.getDeclaredField(name);
		field.setAccessible(true);
		field.set(plugin, value);
	}

	private void focus(boolean focused)
	{
		FocusChanged event = new FocusChanged();
		event.setFocused(focused);
		plugin.onFocusChanged(event);
	}

	private void changeConfig()
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup(FocusMutePlugin.CONFIG_GROUP);
		plugin.onConfigChanged(event);
	}

	private AreaSoundEffectPlayed areaSound()
	{
		AreaSoundEffectPlayed event = new AreaSoundEffectPlayed(null);
		plugin.onAreaSoundEffectPlayed(event);
		return event;
	}

	@Test
	public void blocksOwnAndOtherPlayersAreaSoundsOnlyWhenUnfocused()
	{
		assertFalse(areaSound().isConsumed());
		focus(false);
		for (Actor actor : new Actor[] {
			mock(Player.class), mock(Player.class), null })
		{
			AreaSoundEffectPlayed event = new AreaSoundEffectPlayed(actor);
			plugin.onAreaSoundEffectPlayed(event);
			assertTrue(event.isConsumed());
		}
		focus(true);
		assertFalse(areaSound().isConsumed());
	}

	@Test
	public void soundAndAreaOptionsAreIndependent()
	{
		when(config.muteAreaSounds()).thenReturn(false);
		focus(false);
		assertFalse(areaSound().isConsumed());
		SoundEffectPlayed event = new SoundEffectPlayed(null);
		plugin.onSoundEffectPlayed(event);
		assertTrue(event.isConsumed());
		when(config.muteSoundEffects()).thenReturn(false);
		when(config.muteAreaSounds()).thenReturn(true);
		changeConfig();
		assertTrue(areaSound().isConsumed());
		event = new SoundEffectPlayed(null);
		plugin.onSoundEffectPlayed(event);
		assertFalse(event.isConsumed());
	}

	@Test
	public void repeatedFocusAndConfigEventsDoNotOverwriteSavedVolumes()
	{
		focus(false);
		focus(false);
		changeConfig();
		focus(true);
		assertEquals(120, music);
		assertEquals(80, sound);
		assertEquals(50, area);
		focus(true);
		assertEquals(50, area);
	}

	@Test
	public void configChangeWhileFocusedDoesNotMute()
	{
		changeConfig();
		verify(client, never()).setMusicVolume(0);
		verify(preferences, never()).setSoundEffectVolume(0);
		verify(preferences, never()).setAreaSoundEffectVolume(0);
	}

	@Test
	public void disablingAreaOptionRestoresOnlyThatChannel()
	{
		focus(false);
		when(config.muteAreaSounds()).thenReturn(false);
		changeConfig();
		assertEquals(50, area);
		assertEquals(0, sound);
		assertEquals(0, music);
		assertFalse(areaSound().isConsumed());
		focus(true);
		assertEquals(80, sound);
	}

	@Test
	public void shutdownRestoresLatestVolumesAndStopsBlocking()
	{
		music = 200;
		sound = 30;
		area = 100;
		focus(false);
		plugin.shutDown();
		assertEquals(200, music);
		assertEquals(30, sound);
		assertEquals(100, area);
		assertFalse(areaSound().isConsumed());
	}

	@Test
	public void shutdownWhileFocusedLeavesUserVolumesAlone()
	{
		area = 37;
		plugin.shutDown();
		assertEquals(37, area);
	}

	@Test
	public void alreadyMutedChannelStaysMutedOnReturn()
	{
		area = 0;
		focus(false);
		focus(true);
		assertEquals(0, area);
	}

	@Test
	public void unrelatedConfigurationDoesNotChangeVolumes()
	{
		ConfigChanged event = new ConfigChanged();
		event.setGroup("music");
		plugin.onConfigChanged(event);
		verify(client, never()).setMusicVolume(anyInt());
		verify(preferences, never()).setAreaSoundEffectVolume(anyInt());
	}

	@Test
	public void startupWhileUnfocusedMutesImmediately()
	{
		plugin.shutDown();
		when(canvas.isFocusOwner()).thenReturn(false);
		plugin.startUp();
		assertTrue(areaSound().isConsumed());
		assertEquals(0, area);
		focus(true);
		assertEquals(50, area);
	}
}
