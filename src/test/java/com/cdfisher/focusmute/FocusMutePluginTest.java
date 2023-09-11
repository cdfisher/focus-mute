package com.cdfisher.focusmute;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class FocusMutePluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(FocusMutePlugin.class);
		RuneLite.main(args);
	}
}