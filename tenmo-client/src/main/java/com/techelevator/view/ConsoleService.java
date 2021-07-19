package com.techelevator.view;


import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.TransferList;
import com.techelevator.tenmo.model.User;
import com.techelevator.tenmo.model.UserListItem;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Scanner;

public class ConsoleService {

	private PrintWriter out;
	private Scanner in;

	public ConsoleService(InputStream input, OutputStream output) {
		this.out = new PrintWriter(output, true);
		this.in = new Scanner(input);
	}

	public Object getChoiceFromOptions(Object[] options) {
		Object choice = null;
		while (choice == null) {
			displayMenuOptions(options);
			choice = getChoiceFromUserInput(options);
		}
		out.println();
		return choice;
	}

	private Object getChoiceFromUserInput(Object[] options) {
		Object choice = null;
		String userInput = in.nextLine();
		try {
			int selectedOption = Integer.valueOf(userInput);
			if (selectedOption > 0 && selectedOption <= options.length) {
				choice = options[selectedOption - 1];
			}
		} catch (NumberFormatException e) {
			// eat the exception, an error message will be displayed below since choice will be null
		}
		if (choice == null) {
			out.println(System.lineSeparator() + "*** " + userInput + " is not a valid option ***" + System.lineSeparator());
		}
		return choice;
	}

	private void displayMenuOptions(Object[] options) {
		out.println();
		for (int i = 0; i < options.length; i++) {
			int optionNum = i + 1;
			out.println(optionNum + ") " + options[i]);
		}
		out.print(System.lineSeparator() + "Please choose an option >>> ");
		out.flush();
	}

	public String getUserInput(String prompt) {
		out.print(prompt+": ");
		out.flush();
		return in.nextLine();
	}

	public Integer getUserInputInteger(String prompt) {
		Integer result = null;
		do {
			out.print(prompt+": ");
			out.flush();
			String userInput = in.nextLine();
			try {
				result = Integer.parseInt(userInput);
			} catch(NumberFormatException e) {
				out.println(System.lineSeparator() + "*** " + userInput + " is not valid ***" + System.lineSeparator());
			}
		} while(result == null);
		return result;
	}

	public void displayUserList(UserListItem[] userList){

		System.out.println("-------------------------------------------");
		System.out.println(String.format("%-20s  %-20s", "ID", "Name"));
		System.out.println("-------------------------------------------");

		for(UserListItem user : userList){
			System.out.println(String.format("%-20s  %-20s",user.getUserId(), user.getUsername()));

		}
		System.out.println("-------------------------------------------");

	}

	public void displayTransferList(TransferList[] transferList, String ColName){

		System.out.println("-------------------------------------------");
		System.out.println(String.format("%-15s  %-15s  %-15s", "ID", ColName , "Amount"));
		System.out.println("-------------------------------------------");

		for( TransferList transferHistory : transferList){
			System.out.println(String.format("%-15s  %-15s  $%-15s", transferHistory.getTransferId(),
					transferHistory.getToFromName() ,transferHistory.getAmount()));

		}
		System.out.println("-------------------------------------------");


	}

	public void displayTransferDetails(Transfer transferDetail){
		System.out.println("-------------------------------------------");
		System.out.println(String.format("%-20s ", "Transfer Details"));
		System.out.println("-------------------------------------------");

		System.out.println("Id: " + transferDetail.getTransferId());
		System.out.println("From: " + transferDetail.getUserFrom());
		System.out.println("To: " + transferDetail.getUserTo());
		System.out.println("Type: " + transferDetail.getTransferType());
		System.out.println("Status: " + transferDetail.getTransferStatus());
		System.out.println("Amount: $" + transferDetail.getAmount());


		System.out.println("-------------------------------------------");

	}



}
