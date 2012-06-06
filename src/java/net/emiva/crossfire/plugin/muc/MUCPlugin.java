package net.emiva.crossfire.plugin.muc;

import java.io.File;

import net.emiva.crossfire.container.Plugin;
import net.emiva.crossfire.container.PluginManager;


public class MUCPlugin implements Plugin {
	private MultiUserChatManager mucManager;
	
	@Override
	public void destroyPlugin() {
		mucManager.stop();
	}

	@Override
	public void initializePlugin(PluginManager pluginManager, File configDirectory) {
		mucManager = new MultiUserChatManager();
		mucManager.start();
	}

	public MultiUserChatManager getMultiUserChatManager()
	{
		return mucManager;
	}
}
