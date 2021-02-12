package DiscordBot.DiscordBot;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public interface IFunService {    
	void HandleMessage(String userTag, String message, MessageChannel channel, String userMention, MessageReceivedEvent event);
}
