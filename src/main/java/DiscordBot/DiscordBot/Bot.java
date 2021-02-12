package DiscordBot.DiscordBot;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
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
	public static String TOKEN;
	public static String DB_URL;
	public static String DB_USERNAME;
	public static String DB_PASSWORD;
	
	public static String query;
	public static Statement statement;
	public static Connection connection;
	
	public static Integer balance; 
	
	private static ArrayList<IFunService> funServices = new ArrayList<IFunService>();
	// private static Trivia trivia = new Trivia();       
	
	public static void main(String[] args) throws LoginException, InterruptedException, IOException, SQLException{
		
		funServices.add(new Quiz());
		
		Scanner file = new Scanner(new File("weeb.txt")).useDelimiter(",");
		fileWordsArray = readFileIntoArray(file);
		
		//Reading config file values into variables
		File configFile = new File("config.properties");
		try {
		    FileReader reader = new FileReader(configFile);
		    Properties props = new Properties();
		    props.load(reader);
		    TOKEN = props.getProperty("TOKEN");
		    DB_URL = props.getProperty("DB_URL");
		    DB_USERNAME = props.getProperty("DB_USERNAME");
		    DB_PASSWORD = props.getProperty("DB_PASSWORD");
		    reader.close();     
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		}
		JDA jda = JDABuilder.createDefault(TOKEN)
		  .addEventListeners(new Bot())
		  .build();
		
		//Connecting to database
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
			System.out.println("Connection to database " + DB_URL + " was successful..");
			
			
			
			/*query = "insert into user(user_tag,coins,level,xp) values('Dahlia#0097',100, 1, 80)";
			statement = connection.createStatement();
            statement.execute(query);*/
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException throwables) {
            throwables.printStackTrace();
        }
	}
	
	public void onReady(ReadyEvent event)
	{
		System.out.println("Dummy is ready :)");
		System.out.println(event.getJDA().getToken());
	}
	
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getAuthor().isBot())
			return;
		else {
			String messageString = event.getMessage().getContentRaw();
			String user_tag = event.getAuthor().getAsTag();
			String userMention = event.getAuthor().getAsMention();
			
			MessageChannel channel = event.getChannel(); 
			
			for (IFunService fun : funServices) {
				fun.HandleMessage(user_tag, messageString, channel, userMention, event);
			}
			
			if (checkMessageForKeywords(messageString) && !event.getAuthor().isBot())
				channel.sendMessage(userMention + " Stop being a weeb").queue();
			
			
			//if users engages with bot by starting the message with keyword 'dummy', check the message for further bot's reponse
			if (messageString.toLowerCase().startsWith("dum "))
			{
				try {
					checkRequest(user_tag, messageString, channel, userMention);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			else return;
		}	
	}
		
	
	//check the request user made to bot and give response accordingly
	public static void checkRequest(String user_tag, String message, MessageChannel channel, String userMention) throws SQLException {
		
		switch(message.toLowerCase()) 
		{
			case "dummy create me":
				createUser(user_tag, channel, userMention);
				break;
			case "dummy give moni":
				giveCoins(user_tag, channel, userMention);
				break;
			case "dummy show moni":
				showBalance(user_tag, channel, userMention);
				break; 
			default:     
				break;
		} 
		  
		
	}
	
	//insert user into database
	public static void createUser(String user_tag, MessageChannel channel, String userMention) throws SQLException 
	{
		if (userExists(user_tag, channel, userMention))
			channel.sendMessage(userMention + " You already exists dumdum!").queue();
		else
		{
		
			query = ("insert into user(user_tag,coins,level,xp) values('"+user_tag+"',0, 1, 0)");
			try {
				statement = connection.createStatement();
				statement.execute(query);
			} catch (SQLException e){
				e.printStackTrace();
			}
			channel.sendMessage(userMention + " Congrats, now you exist!").queue();
			
		}
	}
	
	//a method to generate a random amount of coins to add to usesr's balance
	public static void giveCoins(String user_tag, MessageChannel channel, String userMention) throws SQLException
	{
		//TODO: Add time stamps and limit the time for running this command
		
		int min = 50;
		int max = 500;
		
		Random random = new Random();
		int randomBalance = random.nextInt(max + 1 - min) + min;
		
		if(randomBalance < 100)
			channel.sendMessage(userMention + " " + randomBalance + " coins is what u get, you dont deserve more").queue();
		else if (randomBalance >= 100 && randomBalance < 300)
			channel.sendMessage(userMention + " You get " + randomBalance + " coins this time, don't start getting greedy!").queue();
		else
			channel.sendMessage(userMention + " I'll give you " + randomBalance + " coins, I'm feeling generous").queue();
		
		insertUserBalance(randomBalance, user_tag, channel, userMention);
	}
	
	//inserting given coins amount into user table
	public static void insertUserBalance(int coins, String user_tag, MessageChannel channel, String userMention) throws SQLException 
	{
		if (userExists(user_tag, channel, userMention))
		{
			int previousBalance = getCoins(user_tag, channel);
			int updatedBalance = previousBalance + coins;
			query = "UPDATE user SET coins = " + updatedBalance + " WHERE user_tag = '" + user_tag + "'";
			
				try {
				statement = connection.createStatement();
				statement.execute(query);
				
				//channel.sendMessage(userMention + " Added " + coins + " coins into your wallet, your current balance is: " +updatedBalance+ " coins.").queue();
			} catch (SQLException e){
				e.printStackTrace();
			}
		}
		else
			channel.sendMessage(userMention + " You do not exist yet, to come into existance type 'dummy create me'").queue();
		return;
	}
	
	//showing user's coins amount
	public static void showBalance(String user_tag, MessageChannel channel, String userMention) throws SQLException
	{	
		if (userExists(user_tag, channel, userMention))
		{
			int coins = getCoins(user_tag, channel);
		
			if (coins >= 0 && coins < 100)
				channel.sendMessage(userMention + " Your current balance is whole " + coins + " coins ...pathetic").queue();
			if (coins >= 100 && balance < 5000)
				channel.sendMessage(userMention + " Your current balance is whole " + coins + " coins ...mediocre").queue();
			else
				channel.sendMessage(userMention + " Your current balance is whole " + coins + " coins").queue();
		}
		else
			channel.sendMessage(userMention + " You do not exist yet, to come into existance type 'dummy create me'").queue();		
	}
	
	//Returns coins amount for the given account
	public static int getCoins(String user_tag, MessageChannel channel) throws SQLException 
	{
		query = ("SELECT coins FROM user WHERE user_tag = '" + user_tag + "'");
		ResultSet resultSet;

		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				balance = resultSet.getInt(1);
			}
		} catch (SQLException e) 
		{
			e.printStackTrace();
		}
		return balance;
	}
	
	//checks if user exists in database
	public static boolean userExists(String user_tag,MessageChannel channel, String userMention) throws SQLException
	{
	
		query = "SELECT 1 FROM user WHERE user_tag = '" + user_tag + "'";
		ResultSet resultSet;
		
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(query);
			if (resultSet.next())
			{
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
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
