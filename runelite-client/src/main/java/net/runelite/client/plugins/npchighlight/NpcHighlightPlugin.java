/*
 * Copyright (c) 2018, James Swindle <wilingua@gmail.com>
 * Copyright (c) 2018, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.npchighlight;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.events.ConfigChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.util.WildcardMatcher;

@PluginDescriptor(name = "NPC Highlight")
public class NpcHighlightPlugin extends Plugin
{
	// Option added to NPC menu
	private static final String TAG = "Tag";

	// Regex for splitting the hidden items in the config.
	private static final String DELIMITER_REGEX = "\\s*,\\s*";

	@Inject
	private Client client;

	@Inject
	private MenuManager menuManager;

	@Inject
	private NpcHighlightConfig config;

	@Inject
	private NpcClickboxOverlay npcClickboxOverlay;

	@Inject
	private NpcMinimapOverlay npcMinimapOverlay;

	@Getter(AccessLevel.PACKAGE)
	private final Set<Integer> npcTags = new HashSet<>();

	@Getter(AccessLevel.PACKAGE)
	private final List<NPC> taggedNpcs = new ArrayList<>();

	@Getter(AccessLevel.PACKAGE)
	private Map<NPC, String> highlightedNpcs = new HashMap<>();

	private void toggleTag(int npcId)
	{
		boolean removed = npcTags.remove(npcId);
		if (!removed)
			npcTags.add(npcId);
	}

	@Provides
	NpcHighlightConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NpcHighlightConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		if (config.isTagEnabled())
			menuManager.addNpcMenuOption(TAG);
	}

	@Override
	protected void shutDown() throws Exception
	{
		npcTags.clear();
		taggedNpcs.clear();
		menuManager.removeNpcMenuOption(TAG);
	}

	@Subscribe
	public void updateConfig(ConfigChanged event)
	{
		if (!event.getGroup().equals("npchighlight"))
			return;

		if (config.isTagEnabled())
			menuManager.addNpcMenuOption(TAG);
		else
			menuManager.removeNpcMenuOption(TAG);
	}

	@Subscribe
	public void onMenuObjectClicked(MenuOptionClicked click)
	{
		if (click.getMenuOption().equals(TAG))
			toggleTag(click.getId());
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		highlightedNpcs = buildNpcsToHighlight();
		taggedNpcs.clear();
		if (npcTags.isEmpty() || !config.isTagEnabled())
		{
			return;
		}
		for (NPC npc : client.getNpcs())
		{
			if (npcTags.contains(npc.getIndex()))
			{
				NPCComposition composition = getComposition(npc);
				if (composition == null || composition.getName() == null)
					continue;

				taggedNpcs.add(npc);
			}
		}
	}

	@Override
	public Collection<Overlay> getOverlays()
	{
		return Arrays.asList(npcClickboxOverlay, npcMinimapOverlay);
	}

	private Map<NPC, String> buildNpcsToHighlight()
	{
		String configNpcs = config.getNpcToHighlight().toLowerCase();
		if (configNpcs.isEmpty())
			return Collections.EMPTY_MAP;

		Map<NPC, String> npcMap = new HashMap<>();
		List<String> highlightedNpcs = Arrays.asList(configNpcs.split(DELIMITER_REGEX));

		for (NPC npc : client.getNpcs())
		{
			NPCComposition composition = getComposition(npc);

			if (npc == null || composition == null || composition.getName() == null)
				continue;

			for (String highlight : highlightedNpcs)
			{
				String name = composition.getName().replace('\u00A0', ' ');
				if (WildcardMatcher.matches(highlight, name))
				{
					npcMap.put(npc, name);
				}
			}
		}

		return npcMap;
	}

	/**
	 * Get npc composition, account for imposters
	 *
	 * @param npc
	 * @return
	 */
	protected NPCComposition getComposition(NPC npc)
	{
		if (npc == null)
			return null;

		NPCComposition composition = npc.getComposition();
		if (composition != null && composition.getConfigs() != null)
		{
			composition = composition.transform();
		}

		return composition;
	}
}
