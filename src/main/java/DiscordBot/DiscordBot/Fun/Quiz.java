package DiscordBot.DiscordBot.Fun;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import DiscordBot.DiscordBot.IFunService;
import DiscordBot.DiscordBot.Question;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.EmbedType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Quiz implements IFunService {
    private static Random rand = new Random();
 
	private final String QUESTIONS_FOLDER = "questions"; 
	private String[] correctMessages = new String[] {
			"That's correct! Good job.",
			"Nice, that's right.", 
			"Your answer is correct.",
			"You're good. That's the right answer."
	}; 
	
	private String[] incorrectMessages = new String[] {
			"The correct answer is ",
	       "That's wrong, the correct one is ",
	       "You butt, the right one is "
	}; 
	
	private ArrayList<String> whitelistedWords = new ArrayList<String>(
		Arrays.asList("fucking", "butt")
	); 
	
	private Question lastQuestion;
	 
	private List<Question> questions;
	private HashMap<String, Question> users = new HashMap<String, Question>();
	
	public Quiz() {
		LoadQuestions();
	} 
	
	private boolean LoadQuestions() {
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(QUESTIONS_FOLDER + "/"
					+ "questions1.json"));
		} catch (FileNotFoundException e) { 
			System.out.println(e.getMessage());
			return false;
		}
		Gson gson = new Gson();
		
		Question[] questions = gson.fromJson(reader, Question[].class);
		this.questions = Arrays.asList(questions);      
		
		return true;
	}
	
	public Question GetQuestion(String userID) {
		if (questions.isEmpty())
			return null;
			
		Question question = questions.get(rand.nextInt(questions.size()));
		users.put(userID, question);
		
		return question;
	}
	
	public boolean IsPending(String userID) {
		return users.containsKey(userID); 
	} 
	
	public String AnswerQuestion(String userID, String answer) { 
		if (!users.containsKey(userID))
			return "";    

		Question question = users.get(userID);
		List<String> userWords = Arrays.asList(answer.split(" ")).stream()
				.map(s -> s.toLowerCase())
				.filter(w -> !whitelistedWords.contains(w))
				.collect(Collectors.toList());
		
		List<String> answers = Arrays.asList(question.getAnswers()).stream().map(s -> s.toLowerCase()).collect(Collectors.toList());
		
		if (answers.stream().anyMatch(a -> Arrays.equals(a.split(" "), userWords.toArray()))) {      
			
			users.remove(userID); 
			return correctMessages[rand.nextInt(correctMessages.length)];
		} 

		users.remove(userID);
		return incorrectMessages[rand.nextInt(incorrectMessages.length)] + question.getAnswers()[0];
	}
	
	private void SendQuestion(Question question, MessageChannel channel, MessageReceivedEvent event) {
		Color color = new Color(rand.nextInt(255), rand.nextInt(255), rand.nextInt(255));

		MessageEmbed embedded = new EmbedBuilder()
			.setAuthor(event.getAuthor().getName() + "'s quiz question", null, event.getAuthor().getAvatarUrl())
			.setColor(color)
			.setDescription(question.getQuestion())
			.addField("Category", "World of Warcraft", true)
			.addField("Points", question.getPoints(), true)
			.setFooter("You have all the time to answer.")
			.build();
		
		lastQuestion = question;
		channel.sendMessage(embedded).queue();
	}

	@Override
	public void HandleMessage(String userTag, String message, MessageChannel channel, String userMention, MessageReceivedEvent event) {
		if (message.toLowerCase().equals("dum quiz")) {
			Question question = GetQuestion(userTag);
			SendQuestion(question, channel, event);
		} 
		else if (message.toLowerCase().equals("dum quiz last")) {
			users.put(userTag, lastQuestion);
			SendQuestion(lastQuestion, channel, event);
		}
		else if (IsPending(userTag) ) {
			String response = AnswerQuestion(userTag, message); 
			channel.sendMessage(userMention + " " + response).queue();
		}
	}
} 
