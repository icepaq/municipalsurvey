package com.anton.municipalsurvey;

import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class WebController {
	
	DatabaseAccess a = new DatabaseAccess();
	
	@RequestMapping(value = "/getuserinfo")
	@ResponseBody
	public String getuserinfo() {
		
		SecurityContext context = SecurityContextHolder.getContext();
		Authentication authentication = context.getAuthentication();
		String username = authentication.getName();
		Object principal = authentication.getPrincipal();
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		
		System.out.println("User: " + authentication.getName());
		System.out.println("Authority: " + authentication.getAuthorities());
		
		return "";
	}
	
	@RequestMapping(value = "/ResultsByCode")
	public String ResultsByCode(@RequestParam String code, Model model) throws SQLException {
		
		model.addAttribute("results", a.getResultsByCode(code));
		model.addAttribute("header", "Entries By " + code);
		return "ResultsByCode";
	}
	
	@RequestMapping(value = "/updateGreeting")
	@ResponseBody
	public String updateGreeting(@RequestParam String greeting) throws SQLException {
		
		return a.updateGreeting(greeting);
	}
	
	@RequestMapping(value = "/newUser")
	@ResponseBody
	public String newUser(@RequestParam String username, @RequestParam String password, @RequestParam String role) throws SQLException {

		return a.newUser(username, password, role);
	}
	
	@RequestMapping(value = "/deleteUser")
	@ResponseBody
	public String deleteUser(@RequestParam String user) throws SQLException {
		
		return a.deleteUser(user);
	}
	
	@RequestMapping(value = "/usermanagement")
	public String usermanagement(Model model) throws SQLException {
		
		model.addAttribute("users", a.getUsers());
		return "usermanagement";
	}
	
	@RequestMapping(value = "/db")
	@ResponseBody
	public String db() throws SQLException {
		
		return a.dbExists();
	}
	
	@RequestMapping(value = "/dbsetup")
	public String dbsetup() { 
		
		return "dbsetup";
	}
	
	@RequestMapping(value = "/surveyComplete")
	@ResponseBody
	public String surveyComplete(@RequestParam String code) throws SQLException {
		
		return a.surveyComplete(code);
	}
	
	@RequestMapping(value = "/updateEndMessage")
	@ResponseBody
	public String updateEndMEssage(@RequestParam String survey, @RequestParam String message) throws SQLException {
		
		return a.updateEndMessage(survey, message);
	}
	
	@RequestMapping(value = "/value")
	@ResponseBody
	public String value(@RequestParam String value) {
		
		System.out.println();
		return "";
	}
	
	@RequestMapping(value = "/results")
	public String results(Model model) throws SQLException {
		
		model.addAttribute("surveys", a.surveys());
		model.addAttribute("results", a.surveyResults());
		model.addAttribute("questions", a.questions());
		model.addAttribute("codes", a.getCodes());
		//a.getQuestions();
		return "results";
	}
	
	@RequestMapping(value = "/login")
	public String login() {
		
		return "login";
	}
	
	//Delete Survey
	@RequestMapping(value = "/delete_survey")
	@ResponseBody
	public String delete_survey(@RequestParam String survey) throws SQLException{
		
		return a.deleteSurvey(survey);
	}
	
	//New survey
	@RequestMapping(value = "/new_survey")
	@ResponseBody
	public String new_survey(@RequestParam String survey_name, @RequestParam String survey_message, @RequestParam String survey_end_message, @RequestParam String survey_end_code) throws SQLException {
		
		System.out.println(survey_end_code);
		return a.addSurvey(survey_name, survey_message, survey_end_message, survey_end_code);
	}
	
	//Home Page
	@RequestMapping(value = "/")
	public String index(Model model) throws SQLException{
			
		model.addAttribute("surveys", a.getSurveys());
		model.addAttribute("greeting", a.getGreeting());
		
		return "index";	
	}
	
	//Survey Page
	@RequestMapping(value = "/survey")
	public String survey(Model model, @RequestParam String survey, HttpServletRequest request) throws SQLException {

		model.addAttribute("questions", a.getQuestions(survey));
		model.addAttribute("survey", survey);
		model.addAttribute("end_message", a.getEndMessage(survey));
		model.addAttribute("code", a.getEndCode(survey, request.getRemoteAddr()));
		
		
		return "survey";
	}
	
	//Survey Management
	@RequestMapping(value = "/manage")
	public String manage(Model model) throws SQLException {
		
		model.addAttribute("surveys", a.getSurveys());
		return "surveys";
	}
	
	@RequestMapping(value = "/survey_control")
	public String create_survey(Model model, @RequestParam String survey) throws SQLException {
		
		model.addAttribute("survey", survey);
		model.addAttribute("questions", a.question_answers(survey)); //Stores answers from selected survey table passing as a GET/POST Parameter
		model.addAttribute("question_answers", a.question_answers(survey)); //Similar to getAnswers but retrieves additional indexes for Thymeleaf tags
		return "editsurvey";
	}
	
	//Processing New Questions
	@RequestMapping(value = "/submit_changes")
	@ResponseBody
	public String submit_changes(@RequestParam String survey, @RequestParam String question_type, @RequestParam String question, @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3, @RequestParam String answer4, @RequestParam String answer5, @RequestParam String answer6) throws SQLException {
		
		return a.newQuestion(survey, question_type, question, answer1, answer2, answer3, answer4, answer5, answer6);
	}
	
	//Submit Answer
	@RequestMapping(value = "/submit_answer")
	@ResponseBody
	public String submit_answer(@RequestParam String question, @RequestParam String answer, @RequestParam String code, @RequestParam String survey, HttpServletRequest request) throws SQLException {
		
		a.submit_answer(request.getRemoteAddr(), question, answer, code, survey);
		System.out.println("WebController.submit_answer 82: " + survey);
		return "SUCCESS";
	}
	
	@RequestMapping(value = "/submit_question_changes")
	@ResponseBody
	public String submit_question_changes(@RequestParam String survey, @RequestParam String question_id, @RequestParam String question, @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3, @RequestParam String answer4, @RequestParam String answer5, @RequestParam String answer6) throws SQLException {
		System.out.println("/submit_question_changes");
		return a.updateQuestion(survey, question_id, question, answer1, answer2, answer3, answer4, answer5, answer6);
	}	
	
	@RequestMapping(value = "/delete_question")
	@ResponseBody
	public String delete_question(@RequestParam String question_id) throws SQLException {
		
		return a.deleteQuestion(question_id);
	}
}
	