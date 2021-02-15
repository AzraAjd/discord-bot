package DiscordBot.DiscordBot;

import com.google.gson.annotations.SerializedName;

public class Question {        
	
	@SerializedName(value = "Question")
	private String question;
	
	@SerializedName(value = "Points")
	private String points;
	
	@SerializedName(value = "Hints")
	private String hints;
	
	@SerializedName(value = "Answers")
	private String[] answers;
	
	public String getQuestion() {
		return question;
	}
	
	public String[] getAnswers() {
		return answers;
	} 
	
	public String getPoints() {
		return points;
	}
	
}
 