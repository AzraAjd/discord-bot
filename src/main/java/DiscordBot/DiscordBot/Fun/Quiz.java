package DiscordBot.DiscordBot.Fun;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import DiscordBot.DiscordBot.IFunService;
import DiscordBot.DiscordBot.Question;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class Quiz implements IFunService {
    private static Random rand = new Random();
 
	private final String QUESTIONS_FOLDER = "questions";
	
	private List<Question> questions;
	private HashMap<String, Question> users = new HashMap<String, Question>();
	
	public Quiz() {
		LoadQuestions();
	} 
	
	private boolean LoadQuestions() {
		JsonReader reader;
		try {
			reader = new JsonReader(new FileReader(QUESTIONS_FOLDER + "\\questions.json"));
		} catch (FileNotFoundException e) { 
			System.out.println(e.getMessage());
			return false;
		}
		Gson gson = new Gson();
		
		Question[] questions = gson.fromJson(reader, Question[].class);
		this.questions = Arrays.asList(questions);      
		
		return true;
	}
	
	public String GetQuestion(String userID) {
		if (questions.isEmpty())
			return "There are no questions";
			
		Question question = questions.get(rand.nextInt(questions.size()));
		users.put(userID, question);
		
		return question.getQuestion();
	}
	
	public boolean IsPending(String userID) {
		return users.containsKey(userID); 
	} 
	
	public String AnswerQuestion(String userID, String answer) { 
		if (!users.containsKey(userID))
			return "";    
		
		Question question = users.get(userID);
		if (Arrays.asList(question.getAnswers()).stream()
				.map(s -> s.toLowerCase())
				.collect(Collectors.toList())
				.contains(answer)) {      
			
			users.remove(userID);
			return "Correct";
		}

		users.remove(userID);
		return "The correct answer is " + question.getAnswers()[0];
	}

	@Override
	public void HandleMessage(String userTag, String message, MessageChannel channel, String userMention, MessageReceivedEvent event) {
		if (message.toLowerCase() == "dum quiz") {
			String question = GetQuestion(userTag);
			channel.sendMessage(userMention + " " + question).queue();
		} 
		else if (IsPending(userTag) ) {
			String response = AnswerQuestion(userTag, message.toLowerCase()); 
			channel.sendMessage(userMention + " " + response).queue();
		}
	}
} 
