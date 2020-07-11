package com.anton.municipalsurvey;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

//This class takes care of all MySQL connections and other logic that has to do with database connections.
public class DatabaseAccess {
	
	public String[][][] surveyResults() throws SQLException {
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/survey_db", "spring_user", "Java_test.");
		ResultSet rs;
		Statement stmt = conn.createStatement();
		
		String[][] surveys = surveys(); // Get a list of surveys
		String[] queries = new String[surveys.length]; //Each query in this array will access a specific survey results table
		
		//Creating the queries to access each table
		for(int i = 0; i < surveys.length; i++) {
			queries[i] = "SELECT * FROM " + surveys[i][0] + "_answers";
			System.out.println(queries[i]);
		}
		
		//Checks the highest amount of entries that any table has
		int entries = 0;
		for(int a = 0; a < surveys.length; a++) {
			try {
				rs = stmt.executeQuery(queries[a]);
				
				int size = 0;
				while(rs.next()) {
					size++;
				}
				
				if(size > entries) {
					entries = size;
				}
				
			}catch(SQLException e) {
				System.out.println(e);
			}
		}
		
		String[][][] results = new String[surveys.length][entries][2]; //Initiating 3D array. [surveys][entries][questions, answers]
		
		//Go over each survey
		for(int b = 0; b < surveys.length; b++) {
			try {
				rs = stmt.executeQuery(queries[b]);
				int counter = 0;
				while(rs.next()) {
					results[b][counter][0] = rs.getString(2);
					results[b][counter][1] = rs.getString(3);
					counter++;
				}
				
			}catch(SQLException e) {
				System.out.print(e);
			}
		}
		
		for(int c = 0; c < results.length; c++) {
			System.out.println("-------------------------------------------------------------");
			for(int d = 0; d < results[c].length; d++) {
				System.out.println(results[c][d][0] + " - " + results[c][d][1]);
			}
		}
		
		
		return results;
	}
	
	public String[][] surveys() throws SQLException {
		
		String query = "SELECT * FROM survey_db.surveys";
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/survey_db", "spring_user", "Java_test.");
		ResultSet rs;
		Statement stmt = conn.createStatement();
		
		int size = 0;
		try {
			rs = stmt.executeQuery(query);
			
			//Find rows in table
			while(rs.next()) {
				size++;
			}
		}
		catch(SQLException e) {
			System.out.println(e);
		}
		
		String[][] surveys = new String[size][2];
		try {
			rs = stmt.executeQuery(query);
			
			int counter = 0;
			while(rs.next()) {
				surveys[counter][0] = rs.getString(2).toUpperCase();
				surveys[counter][1] = "tab(survey_id=" + Integer.toString(counter) + ")";
				counter++;
			}
		} catch(SQLException e) {
			System.out.println(e);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return surveys;
	}
	
	//Deletes a survey by removing the survey entry from the surveys table and, dropping the survey's table.
	public String deleteSurvey(String survey) throws SQLException {
		String query = "DELETE FROM surveys WHERE survey_name = ?"; //Delete Survey Entry
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/survey_db", "spring_user", "Java_test.");
		PreparedStatement stmt = conn.prepareStatement(query);
		
		stmt.setString(1, survey);
		
		try {
			stmt.executeUpdate(); 
		}catch(SQLException e) {
			System.out.println(e);
		}
		
		String query2 = "DROP TABLE " + survey; //Drop survey's table
		try {
			stmt.executeUpdate(query2);
		}catch(SQLException e) {
			System.out.println(e);
		}
		
		String query3 = "DROP TABLE " + survey + "_answers"; //Drop survey's table
		try {
			stmt.executeUpdate(query3);
		}catch(SQLException e) {
			System.out.println(e);
		}finally {
			if(conn != null) {
				conn.close();
			}
		}
		return "";
	}
	
	//Create a survey but check for duplicates before
	public String addSurvey(String name, String message) throws SQLException {
		
		/* Checks if `name` is a duplicate entry */
		String queryEntryCheck = "SELECT * FROM surveys WHERE survey_name = ?";
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/survey_db", "spring_user", "Java_test.");
		PreparedStatement EntryCheckStatement = conn.prepareStatement(queryEntryCheck);
		ResultSet rs;
		
		EntryCheckStatement.setString(1, name);
		
		try {
			rs = EntryCheckStatement.executeQuery();
			
			//If the entry is a duplicate then abort
			if(rs.isBeforeFirst()) {
				System.out.println("Duplicate Survey Entry");
				conn.close();
				return "ERROR - DUPLICATE ENTRY";
			}
		}catch(SQLException e) {
			System.out.println("Line 51: " + e);
		}
		
		
		/* Create a survey entry */
		String query = "INSERT INTO surveys VALUES(NULL, ?, ?)";
		PreparedStatement stmt = conn.prepareStatement(query);
		
		stmt.setString(1, name);
		stmt.setString(2, message);
		
		try {
			System.out.println("Line 48: " + query);
			stmt.executeUpdate();
		}catch(SQLException e) {
			System.out.println(e);
		}
		
		/* Create a table for the survey questions*/
		String query2 = "CREATE TABLE " + name + "(question_id INT PRIMARY KEY AUTO_INCREMENT NOT NULL, question_mc BOOL, question VARCHAR(60), answers VARCHAR(100))";
		
		try {
			System.out.println("Line 57: " + query2);
			stmt.executeUpdate(query2);
		}catch(SQLException e) {
			System.out.println(e);
		}
		
		/* Create table for survey answers */
		Statement stmt2 = conn.createStatement();
		//CHANGE MADE: July 11, 2020. Changing Table Structure. Old table; question_id, answer_id were INT. Now they are VARCHAR.
		String query3 = "CREATE TABLE " + name.toLowerCase() + "_answers(entry_id INT PRIMARY KEY AUTO_INCREMENT, question_id VARCHAR(8), answer_id VARCHAR(8) NOT NULL, survey VARCHAR(60) NOT NULL, user_id VARCHAR(60), time datetime NOT NULL)";
		
		try {
			System.out.println("Line 97: " + query3);
			stmt2.executeUpdate(query3);
		}catch(SQLException e) {
			System.out.println(e);
		}finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		
		return "SUCCESS";
	}

	//Get all surveys
	public String[][] getSurveys() throws SQLException {
		
		String query = "SELECT * FROM survey_db.surveys";
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		int size = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			//Find rows in table
			while(rs.next()) {
				size++;
			}
			
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		String[][] surveys = new String[size][4];
		
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);
			
			int loop_counter = 0;
			while(rs.next()) {
				surveys[loop_counter][0] = rs.getString(2);
				surveys[loop_counter][1] = rs.getString(3);
				
				//These two variables are used for th:attr due to String limitations in Thymeleaf
				surveys[loop_counter][2] = "survey_select('" + surveys[loop_counter][0] + "')";
				surveys[loop_counter][3] = "survey_delete('" + surveys[loop_counter][0] + "')";
				
				loop_counter++;
			}
		} catch(SQLException e) {
			System.out.println(e);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return surveys;
	}
	
	//Delete a question
	public String deleteQuestion(String question_id) throws SQLException {
		
		String query = "DELETE FROM survey_db.questions WHERE question_id = ?";
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		
		PreparedStatement stmt = conn.prepareStatement(query);
		
		stmt.setString(1, question_id);
		
		try{
			stmt.executeUpdate();
		}catch(SQLException e) {
			System.out.println(e);
			return "SQL_ERROR";
		}finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return "SUCCESS";
	}
	
	//Change properties of a question
	public String updateQuestion(String survey, String question_id, String question, String answer1, String answer2, String answer3, String answer4, String answer5, String answer6) throws SQLException {
		
		String answer = answer1 + "|" + answer2 + "|" + answer3 + "|" + answer4 + "|" + answer5 + "|" + answer6; //Concatenating several variables into one string for database
		String query = "UPDATE survey_db." + survey + " SET question = ?, answers = ? WHERE question_id = ?";
		System.out.println(query);
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		PreparedStatement stmt = conn.prepareStatement(query);
		
		stmt.setString(1, question);
		stmt.setString(2, answer);
		stmt.setString(3, question_id);
		
		try {
			stmt.executeUpdate();
		}catch(SQLException e) {
			System.out.println(e);
			return "SQL_ERROR";
		}finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return "SUCCESS";
	}
	
	//Get all questions. Gives more information than just database values/
	public String[][] question_answers(String survey) throws SQLException {
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		//Finding rows of result
		int size = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM survey_db." + survey);
			
			//Find rows in table
			while(rs.next()) {
				size++;
			}
			
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		String[][] questions = new String[size][6];
		
		//Appending table values into 2D array
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM survey_db." + survey);
			
			int loop_counter = 0;
			while(rs.next()) {
				questions[loop_counter][0] = rs.getString(1);
				questions[loop_counter][1] = rs.getString(2);
				questions[loop_counter][2] = rs.getString(3);
				questions[loop_counter][3] = rs.getString(4);
				
				questions[loop_counter][4] = questions[loop_counter][0] + "_question"; //This variable is used to identify the question being referenced in /create_survey
				questions[loop_counter][5] = questions[loop_counter][0] + "_answer";
				
				loop_counter++;
			}
		} catch(SQLException e) {
			System.out.println(e);
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return questions;
		
	}
	
	//Submit an answer selected in a survey
	public String submit_answer(String user_id, String question, String answer, String survey) throws SQLException {
		
		String query = "INSERT INTO survey_db." + survey + "_answers VALUES(NULL, '" + question + "', '" + answer + "', '" + survey + "', '" + user_id + "', NOW());";
		System.out.println(query);
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		Statement stmt = conn.createStatement();
		
		try {
			stmt.executeUpdate(query);	
		}catch(SQLException e) {
			System.out.println(e);
			return "SQL_ERROR";
		}finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		return "SUCCESS";
	}
	
	//Submit a new question
	public String newQuestion(String survey, String question, String answer1, String answer2, String answer3, String answer4, String answer5, String answer6) throws SQLException {
		String query = "INSERT INTO survey_db." + survey + " VALUES(NULL, 1, '" + question + "', " + "'" + answer1 + "|" + answer2 + "|"  + answer3 + "|" + answer4 + "|" + answer5 + "|" + answer6 + "');";
		System.out.println("Line 249: " + query);
		
		
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		Statement stmt = conn.createStatement();
		
		try {
			stmt.executeUpdate(query);
		} 	catch(SQLException e) {
			System.out.println(e);
			return "SQL_ERROR";
		} finally {
			if(conn != null) {
				conn.close();
			}
		}
		
		
		return "SUCCESS";
	}
	
	public String[][] getAnswers(String survey) throws SQLException {
		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "spring_user", "Java_test.");
		Statement stmt = conn.createStatement();
		ResultSet rs;
		
		//Finding rows of result
		int size = 0;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM survey_db." + survey);
			
			//Find rows in table
			while(rs.next()) {
				size++;
			}
			
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		String[][] questions = new String[size][4];
		
		//Appending table values into 2D array
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM survey_db." + survey);
			
			int loop_counter = 0;
			while(rs.next()) {
				questions[loop_counter][0] = rs.getString(1);
				questions[loop_counter][1] = rs.getString(2);
				questions[loop_counter][2] = rs.getString(3);
				questions[loop_counter][3] = rs.getString(4);
				
				loop_counter++;
			}
		} catch(SQLException e) {
			System.out.println(e);
		}
		
		
		
		String[][] question_answers = new String[size][7]; //This array will be passed into Thymeleaf with the question and answers
		String[] temp_answers = new String[6]; //Temporary holder for when String or answers will be split
		
		
		for(int row = 0; row < size; row++) {
			
			for(int column = 0; column < 2; column++) {
				
				question_answers[row][0] = questions[row][2]; //Make the first index of the sub-array the question
				temp_answers = questions[row][3].split("\\|"); //Splitting up string of answers into an array
				
				for(int answers = 1; answers < 7; answers++) {
					
					//Try Catch will ignore out of bounds exceptions if temp_answers is too small
					try {
						question_answers[row][answers] = temp_answers[answers-1]; //Adding answers to the array;
					} catch(ArrayIndexOutOfBoundsException e) {
						System.out.println(e);
					}
				}
			}
		}
		
		for(int a = 0; a < question_answers.length; a++) {
			for(int b = 0; b < 7; b++) {
				System.out.println(question_answers[a][b]);
			}
			System.out.println();
			System.out.println();
		}
		if(conn != null) {
			conn.close();
		}
		
		return question_answers;
	}

}
