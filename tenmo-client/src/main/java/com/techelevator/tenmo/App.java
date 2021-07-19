package com.techelevator.tenmo;

import com.techelevator.tenmo.Exceptiom.TransferServiceException;
import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import com.techelevator.view.ConsoleService;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String PENDING_MENU_OPTION_APPROVE = "Approve";
	private static final String PENDING_MENU_OPTION_REJECT = "Reject";
	private static final String PENDING_MENU_OPTION_DO_NOT_REJECT_OR_APPROVE = "Don't approve or reject";
	private static final String[] PENDING_MENU_OPTIONS = {PENDING_MENU_OPTION_APPROVE, PENDING_MENU_OPTION_REJECT, PENDING_MENU_OPTION_DO_NOT_REJECT_OR_APPROVE};


    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private AccountService accountService;
    private TransferService transferService;
	private UserService userService;

    public static void main(String[] args) throws AuthenticationServiceException {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),
				new AccountService(API_BASE_URL), new TransferService(API_BASE_URL), new UserService(API_BASE_URL));
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, AccountService accountService,
               TransferService transferService, UserService userService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.accountService = accountService;
		this.transferService = transferService;
		this.userService = userService;
	}

	public void run() throws AuthenticationServiceException {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() throws AuthenticationServiceException {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	private void viewCurrentBalance() throws AuthenticationServiceException {
		Long id = Long.valueOf(currentUser.getUser().getId());
		System.out.println("Your current account balance is: $" + accountService.getBalance(id, currentUser.getToken()));

	}

	private void viewTransferHistory() {


    	boolean running = true;
    	while(running){

			try {
				TransferList[] list = transferService.listTransactionHistory(currentUser);
				String colName = "From/To";
				console.displayTransferList(list, colName);
			} catch (TransferServiceException e) {
			e.printStackTrace();
			}

			//user input ....transfer_id to display detailed transfer history of a transfer
			String transferId = console.getUserInput("Please enter transfer ID to view details (0 to cancel)");
			if(transferId.equals("0")){
				running = false;
				return;
			}

			if(!(isValidId(transferId))){
				System.out.println("Invalid Transfer Id.");
				continue;
			}

			Long id = Long.valueOf(transferId);

			try {
				console.displayTransferDetails((transferService.getTransferDetails(currentUser.getToken(), id)));
			} catch (TransferServiceException e) {
				System.out.println(e.getMessage());

			}
		}

    }




	private void viewPendingRequests() {

    	boolean running = true;
    	while(running) {


			try {

				TransferList[] list = transferService.pendingTransactionList(currentUser);
				String colName = "To";
				console.displayTransferList(list, colName);
			} catch (TransferServiceException e) {
				e.printStackTrace();
			}

			String transferId = console.getUserInput("Please enter transfer ID to approve/reject (0 to cancel)");

			if(transferId.equals("0")){
				running = false;
				return;
			}

			if(!(isValidId(transferId))){
				System.out.println("Invalid Transfer Id.");
				continue;
			}

			Long transactionId = Long.valueOf(transferId);
			String choice = (String) console.getChoiceFromOptions(PENDING_MENU_OPTIONS);
			if (choice.equals("Don't approve or reject")) {
				return;
			}
			String authToken = currentUser.getToken();
			try {
				transferService.approveOrReject(transactionId, choice, currentUser);
			} catch (TransferServiceException e) {
				System.out.println(e.getMessage());
			}
		}
	}


	private void sendBucks() {

    	boolean running = true;
		while(running)
		{
		//display userList
		try {
			console.displayUserList(userService.listUsers(currentUser));
		} catch (AuthenticationServiceException e) {
			System.out.println(e.getMessage());
		}

		//user input ...user_id and amount to transfer
		String UserToId = console.getUserInput("Enter ID of user you are sending to (0 to cancel)");

		if (UserToId.equals("0")){
			running = false;
			return;
		}

			if(!(isValidId(UserToId))){
				System.out.println("Invalid Id.");
				continue;
			}


		String amount = console.getUserInput("Enter amount");
			if(!(isNumber(amount))){
				System.out.println("Invalid amount.");
				continue;
			}
		BigDecimal amountToSend = new BigDecimal(amount);

			if(!(amountToSend.compareTo(BigDecimal.ZERO) == 1)){
				System.out.println("Invalid amount.");
				continue;
			}
		Long toId = Long.parseLong(UserToId);

		//make transfer
		try {
			transferService.makeTransfer(currentUser, toId, amountToSend);
		} catch (TransferServiceException e) {
			System.out.println(e.getMessage());
		}
	}
	}


	private void requestBucks() {

    	boolean running = true;
    	while(running) {
			//display userList
			try {
				console.displayUserList(userService.listUsers(currentUser));
			} catch (AuthenticationServiceException e) {
				System.out.println(e.getMessage());

			}

			//user input ...user_id and amount to transfer
			String userToId = console.getUserInput("Enter ID of user you are requesting from (0 to cancel)");

			if(userToId.equals("0")){
				running = false;
				return;
			}

			if(!(isValidId(userToId))){
				System.out.println("Invalid Id.");
				continue;
			}

			String amount = console.getUserInput("Enter amount");


			if(!(isNumber(amount)) ){
				System.out.println("Invalid amount.");
				continue;
			}

			Long toId = Long.parseLong(userToId);
			BigDecimal amountToRequest = new BigDecimal(amount);

			if(!(amountToRequest.compareTo(BigDecimal.ZERO) == 1)){
				System.out.println("Invalid amount.");
				continue;
			}

			//make transfer
			try {
				transferService.makeRequest(currentUser, toId, amountToRequest);
			} catch (TransferServiceException e) {
				System.out.println(e.getMessage());
			}
		}
		
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}


	private boolean isNumber(String amount){

			//create regular expression for decimal number and compile it.
    		String regExp = "[0-9]+(\\.[0-9][0-9]?)?";
    		Pattern p = Pattern.compile(regExp);

    		if(amount == null || amount.equals("")){
    			return false;
			}

    		//find match using pattern matcher
			Matcher m = p.matcher(amount);

    		return m.matches();
	}

	private boolean isValidId(String id){

		//create regular expression for transferId and compile it.
		//^\+?[1-9]\d*$
		String regExp = "^\\+?[1-9]\\d*$";

		Pattern p = Pattern.compile(regExp);

		if(id == null || id.equals("")){
			return false;
		}

		//find match using pattern matcher
		Matcher m = p.matcher(id);

		return m.matches();
	}

}
