package DiscordBot.DiscordBot;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;


public class Trivia {
    private static Random rand = new Random();
 
	private final String QUESTIONS_FOLDER = "questions";
	
	private List<Question> questions;
	private HashMap<String, Question> users = new HashMap<String, Question>();
	
	public Trivia() {
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
		
		return question.GetQuestion();
	}
	
	public boolean IsPending(String userID) {
		return users.containsKey(userID);
	}
	
	public String AnswerQuestion(String userID, String answer) { 
		if (!users.containsKey(userID))
			return "";    
		
		Question question = users.get(userID);
		if (Arrays.asList(question.GetAnswers()).stream()
				.map(s -> s.toLowerCase())
				.collect(Collectors.toList())
				.contains(answer)) {      
			
			users.remove(userID);
			return "Correct";
		}

		users.remove(userID);
		return "The correct answer is " + question.GetAnswers()[0];
	}
} 
