package DiscordBot.DiscordBot;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.security.auth.login.LoginException;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Bot extends ListenerAdapter{
	
	public static String[] fileWordsArray;
	
	public static void main(String[] args) throws LoginException, InterruptedException, FileNotFoundException {
		
		Scanner file = new Scanner(new File("weeb.txt")).useDelimiter(",");
		fileWordsArray = readFileIntoArray(file);

		JDA jda = JDABuilder.createDefault("ODA4NzM2MDM4NzgzOTQyNjU2.YCK4IQ.NF_o1rrs2KTtB8GGMnchxmRsTHI")
		  .addEventListeners(new Bot())
		  .build();
		
	}
	
	public void onReady(ReadyEvent event)
	{
		System.out.println("Dummy is ready :)");
		System.out.println(event.getJDA().getToken());
	}
	
	public void onMessageReceived(MessageReceivedEvent event)
	{
		String messageString = event.getMessage().getContentRaw();
		MessageChannel channel = event.getChannel();
		
		if (checkMessageForKeywords(messageString))
			{
				if (event.getAuthor().isBot())
					return;
				else
					channel.sendMessage("Stop being a weeb").queue();

	       }
		else
			return;
	}
	
	public static boolean checkMessageForKeywords (String message)
	{
		 for(int i =0; i < fileWordsArray.length; i++)
		    {
		        if(message.toLowerCase().contains(fileWordsArray[i]))
		        {
		            return true;
		        }
		    }
		    return false;
	}
	
	public static String[] readFileIntoArray(Scanner file) 
	{
		 List<String> wordList = new ArrayList<String>();
		 String word = "";
		 
		 while (file.hasNext()) {
		     
		      word = file.next();
		      wordList.add(word);
		    }
		 file.close();

		    String[] wordArray = wordList.toArray(new String[0]);
			return wordArray;

	}
}
