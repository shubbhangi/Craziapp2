package com.example.demo;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.auth.UserRecord.CreateRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Empty;
import com.example.demo.model.UserRegister;
import com.example.demo.repo.UserRegisterRepository;
import com.example.demo.service.IUserRegisterService;
import com.example.demo.util.ResponseObject;


@RestController
@RequestMapping("/userRest")
public class UserRegisterController {

	
	static String FB_BASE_URL="https://craziapp-3c02b.firebaseio.com";
	
	@Autowired
	private IUserRegisterService userRegisterService;
	
	@Autowired
	ResponseObject response;
	
	@Autowired
	Empty empty;

	@Autowired
	UserRegisterRepository userRegisterRepository; 
	
	@Autowired
	private EntityManager entitymanager;



	@GetMapping("/myprofile/{userId}")
	public ResponseEntity<ResponseObject> getUser(@PathVariable(value = "userId", required=false) String userid) {
		
		if(userid == null) {
			
			response.setError("1");
			response.setMessage("'userId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else {
			
			int userId = 0;
			List<UserRegister> register = new ArrayList<UserRegister>();
			try {
				
				userId = Integer.parseInt(userid);
				register = userRegisterService.getById(userId);
				
			} catch (NumberFormatException e) {
				
				response.setError("1");
				response.setMessage("wrong userId please enter numeric value");
				response.setData(empty);
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);
				
			}
			
			if(!register.isEmpty()) {
			response.setMessage("your data is retrived successfully");
			response.setData(register);
			response.setError("0");
			response.setStatus("success");
			return ResponseEntity.ok(response);	
			}
			else {
				response.setMessage("no data found");
				response.setData(empty);
				response.setError("0");
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);		
			}
			
		}
	}
	
	
	@SuppressWarnings("unused")
	@PostMapping("/login")
	public ResponseEntity<ResponseObject> loginCredential(@RequestParam(value ="user", required=false) String user,
			@RequestParam(value ="pass", required=false) String pass)
			throws ResourceNotFoundException {
		if(user == null) {
			
			response.setError("1");
			response.setMessage("'user' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else if(pass == null) {
			
			response.setError("1");
			response.setMessage("'pass' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else {
				UserRegister userRegister=null;
				List<UserRegister> userRegisterList = userRegisterService.findByUserName(user);
				
				/*get from database*/
				
					if(userRegisterList.isEmpty())
					{
						List<UserRegister> userList = userRegisterService.findByMobileNumber(user);
						if(userList.isEmpty())
						{
							
						
						response.setMessage("user not found please try again");
						response.setError("1");
						response.setStatus("FAIL");
						response.setData(empty);
						return ResponseEntity.ok(response);
					}
					else {
						userRegister = userList.get(0);
						String name = userRegister.getUserName();
						String mobileNumber=userRegister.getMobilNumber();
						String password = userRegister.getPassword();
						Boolean userStatus=userRegister.getIsActive();
						int userID=userRegister.getUserId();
					
					
					if (mobileNumber.equals(user)  && password.equals(pass) && userStatus.equals(true)) 
					{
						response.setStatus("SUCCESS");
						response.setMessage("Logged in successfully");
						response.setError("0");
						response.setData(userList);
						return ResponseEntity.ok(response);
						
					}else {
						 response.setStatus("FAIL");
						response.setMessage("please check username or password");
						response.setError("1");
						response.setData(empty);
						return ResponseEntity.ok(response);
					}
				}
		}
					else {
						userRegister = userRegisterList.get(0);
						String name = userRegister.getUserName();
						String password = userRegister.getPassword();
						Boolean userStatus=userRegister.getIsActive();
						int userID=userRegister.getUserId();
						
						
						
					if (name.equals(user)  && password.equals(pass) && userStatus==true) 
					{
						response.setStatus("SUCCESS");
						response.setMessage("Logged in successfully");
						response.setError("0");
						response.setData(userRegisterList);
						return ResponseEntity.ok(response);
						
					}else {
						 response.setStatus("FAIL");
						response.setMessage("please check username or password");
						response.setError("1");
						response.setData(empty);
						return ResponseEntity.ok(response);
					}
				}
			}
		}

	
	@PostMapping("/saveall")
	public ResponseEntity<ResponseObject> addUser(@RequestParam (value ="userName", required=false)String userName,
			@RequestParam (value ="password", required=false)String password,
			@RequestParam (value ="mobileNumber", required=false)String mobileNumber) {
		
		if(userName == null) {
			
			response.setError("1");
			response.setMessage("'userName' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else if(password == null) {
			
			response.setError("1");
			response.setMessage("'password' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else if(mobileNumber == null) {
			
			response.setError("1");
			response.setMessage("'mobileNumber' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else {
		
				UserRegister user = new UserRegister();
				
		
				user.setUserName(userName);
				user.setPassword(password);
				user.setMobilNumber(mobileNumber);
				user.setIsActive(true);
				user.setSourceFrom("Laptop");
				
	
				
				user.setToken(getRandomNumber());
				user.setCreateDate(getDate());
				user.setLastModifiedDate(getDate());
				
				
				
				if(!userExists(mobileNumber) && !userNameExists(userName)){
					
					userRegisterService.save(user);
					
				//	sms.sendSms(String.valueOf(mobileNumber), "Your CraziApp Registration is successful enter OTP to verify : "+OTP);
					
					response.setStatus("Success");
					response.setMessage("CraziApp Registration is successful");
					response.setError("0");
					response.setData(user);
					
					return ResponseEntity.ok(response);
					
				}
				else {
					
					response.setStatus("FAIL");
					response.setMessage(" user is allready Registered");
					response.setError("1");
					response.setData(empty);
						return ResponseEntity.ok(response);
				}
			}
		}
		

		//@Cacheable(value="mobileNumberCache",key="#mobileNumber",unless="#result==null")
		private boolean userExists(String mobileNumber)
		{
			String hql="FROM UserRegister as ur WHERE ur.mobilNumber= ?1";
			int count=entitymanager.createQuery(hql).setParameter(1, mobileNumber).getResultList().size();
			
			return count>0 ? true : false;
			
		}
		
		//@Cacheable(value="userNameCache",key="#userName",unless="#result==null")
		private boolean userNameExists(String userName)
		{
			String hql="FROM UserRegister as ur WHERE ur.userName= ?1";
			int count=entitymanager.createQuery(hql).setParameter(1, userName).getResultList().size();
			
			return count>0 ? true : false;
			
		}
		
		private LocalDateTime getDate() {
	
			LocalDateTime now = LocalDateTime.now();
	
			return now;
	
		}

		private int getRandomNumber() {
	
			int rand = new Random().nextInt(10000); 
			  
			return rand;
	
		}
	
	
	@PostMapping("/ptrn/unlk")
	public ResponseEntity<ResponseObject> patternCredential(@RequestParam(value ="userId", required=false) String userId,
			@RequestParam(value ="pattern", required=false) String pattern)
			throws ResourceNotFoundException {
		
		if(userId == null) {
			
			response.setError("1");
			response.setMessage("'userId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else if(pattern == null) {
			
			response.setError("1");
			response.setMessage("'pattern' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else  {
				
				int userid = 0;
				try {
					userid = Integer.parseInt(userId);
				} catch (NumberFormatException e) {

					response.setError("1");
					response.setMessage("wrong userId please enter numeric value");
					response.setData(empty);
					response.setStatus("FAIL");
					return ResponseEntity.ok(response);

				}
				
				UserRegister userRegister = userRegisterService.getOneById(userid);

						String dbpattern = userRegister.getPattern();
					
					if (dbpattern.equals(pattern) || dbpattern == pattern) 
					{
						response.setStatus("SUCCESS");
						response.setMessage("Logged in successfully");
						response.setError("0");
						response.setData(userRegister);
						return ResponseEntity.ok(response);
						
					}else {
						 response.setStatus("FAIL");
						response.setMessage("pattern does not matched please check");
						response.setError("1");
						response.setData(empty);
						return ResponseEntity.ok(response);
					}
				
			}
		}

	
	@PostMapping(value = {"/ptrn/save","/ptrn/update"})
	public ResponseEntity<ResponseObject> patternSave(@RequestParam(value ="userId", required=false) String userId,
			@RequestParam(value ="pattern", required=false) String pattern)
			throws ResourceNotFoundException {
		
		if(userId == null) {
			
			response.setError("1");
			response.setMessage("'userId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else if(pattern == null) {
			
			response.setError("1");
			response.setMessage("'pattern' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else  {
				
				int userid = 0;
				try {
					userid = Integer.parseInt(userId);
				} catch (NumberFormatException e) {

					response.setError("1");
					response.setMessage("wrong userId please enter numeric value");
					response.setData(empty);
					response.setStatus("FAIL");
					return ResponseEntity.ok(response);

				}
				
				UserRegister userRegister = userRegisterService.getOneById(userid);

					userRegister.setPattern(pattern);
					
					userRegisterService.save(userRegister);
					
					response.setStatus("SUCCESS");
					response.setMessage("Pattern updated successfully");
					response.setError("0");
					response.setData(userRegister);
					return ResponseEntity.ok(response);
						
			}
		}

	
	@PostMapping(value = {"/ptrn/remove"})
	public ResponseEntity<ResponseObject> patternRemove(@RequestParam(value ="userId", required=false) String userId)
			throws ResourceNotFoundException {
		
		if(userId == null) {
			
			response.setError("1");
			response.setMessage("'userId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
			
		}else {
				
				int userid = 0;
				try {
					userid = Integer.parseInt(userId);
				} catch (NumberFormatException e) {

					response.setError("1");
					response.setMessage("wrong userId please enter numeric value");
					response.setData(empty);
					response.setStatus("FAIL");
					return ResponseEntity.ok(response);

				}
				
				UserRegister userRegister = userRegisterService.getOneById(userid);

					userRegister.setPattern(null);
					
					userRegisterService.save(userRegister);
					
					response.setStatus("SUCCESS");
					response.setMessage("pattern removed successfully");
					response.setError("0");
					response.setData(userRegister);
					return ResponseEntity.ok(response);
						
			}
		}

}
