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
	
	@GetMapping("/userprofile/{userId}")
	public ResponseEntity<ResponseObject> getUserProfile(@PathVariable(value = "userId", required=false) String userid) {
		
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
				
				UserRegister userRegister = register.get(0);
				
				GetProfile getProfile = new GetProfile();
				
				getProfile.setUserId(userId);
				getProfile.setUserName(userRegister.getUserName());
				getProfile.setCraziId(userRegister.getUserName());
				getProfile.setProfilePath(userRegister.getProfile().getCurrentProfile());
				getProfile.setDisplayName(userRegister.getProfile().getDisplayName());
				getProfile.setAbooutUser(userRegister.getProfile().getAboutUser());
				getProfile.setGroupList(groupProfileService.getUserGroupdetailByUserId(userRegister.getMobilNumber()));
				getProfile.setBookmarkList(mediaFileService.getBookmarksByUserId(userId));
				List<MediaFiles> profileList = fileStorageService.getAllProfileById(userId);
				getProfile.setPhotos(mediaFileService.getAllProfileCountById(userId));
				Double rating = commonUtil.getRating(profileList);
				
		       
				Long likes = commonUtil.getLikes(profileList);
				getProfile.setRating((rating));
				getProfile.setLikes(likes);
	//			getProfile.setUserProfile(userProfile);
				getProfile.setProfileList(profileList);
			response.setMessage("your data is retrived successfully");
			response.setData(getProfile);
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
		
			try {
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials
								.fromStream(new ClassPathResource("/craziapp-3c02b-firebase-adminsdk-rrs6o-3add9ace15.json").getInputStream()))
						.setDatabaseUrl(FB_BASE_URL).build();
				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			
				UserRegister user = new UserRegister();
				UserContact userContact = new UserContact();
				UserProfile profile = new UserProfile();
				//MediaFiles mediaFiles=new MediaFiles();
				Biometric biometric=new Biometric();
		
				user.setUserName(userName);
				user.setPassword(password);
				user.setMobilNumber(mobileNumber);
				user.setIsActive(true);
				user.setSourceFrom("Laptop");
				List<UserContact> userContactList=new ArrayList<>();
				
				userContact.setContactNumber(mobileNumber);
				userContact.setContactName(userName);
				userContact.setCreateDate(getDate());
	
				userContactList.add(userContact);
				user.setUserContactList(userContactList);
				user.setToken(getRandomNumber());
				user.setCreateDate(getDate());
				user.setLastModifiedDate(getDate());
				profile.setDisplayName(user.getUserName());
				
				//profile.getFiles().add(mediaFiles);
				biometric.setIsActive(true);
				user.setProfile(profile);
				user.getBiometric().add(biometric);	
				UserOtp userOtp = new UserOtp();
				
				
				if(!userExists(mobileNumber) && !userNameExists(userName)){
					
					userOtp.setIs_active(true);
					userOtp.setCreateDate(getDate());
					userOtp.setLastModifiedDate(getDate());
					int OTP = util.getOTP();
					userOtp.setOtp(OTP);
					user.setUserOtp(userOtp);
		
					userRegisterService.save(user);
					
					try {
						CreateRequest request = new CreateRequest()
								.setUid(user.getUserName())
								.setPassword(user.getPassword())
								.setPhoneNumber(user.getMobilNumber())
								.setDisplayName(user.getUserName())
								.setPhotoUrl("https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png")
								.setDisabled(false);
						
						UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
						System.out.println("Successfully created new user: " + userRecord.getUid());
						
						Map<String, Object> users = new HashMap<>();
						users.put(user.getUserName(), new UserFirebase(user.getUserId(),user.getUserName(), "https://upload.wikimedia.org/wikipedia/commons/8/89/Portrait_Placeholder.png", user.getUserName(),user.getIsActive().toString()));
					
						//ref.child("group").setValueAsync("group", new GroupFirebase(groupProfile.getGroupId(), groupProfile.getDisplayName(), userRegister.getUserName(), groupProfile.getGroupMember()));
						//ref.getDatabase().getReference().push().c.getReferenceFromUrl(FB_BASE_URL).setValueAsync(groups);
						database.getReference().child("Usres").updateChildrenAsync(users);
					} catch (FirebaseAuthException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
					sms.sendSms(String.valueOf(mobileNumber), "Your CraziApp Registration is successful enter OTP to verify : "+OTP);
					
					response.setStatus("Success");
					response.setMessage("  CraziApp Registration is successful");
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
		

		@Cacheable(value="mobileNumberCache",key="#mobileNumber",unless="#result==null")
		private boolean userExists(String mobileNumber)
		{
			String hql="FROM UserRegister as ur WHERE ur.mobilNumber= ?1";
			int count=entitymanager.createQuery(hql).setParameter(1, mobileNumber).getResultList().size();
			
			return count>0 ? true : false;
			
		}
		
		@Cacheable(value="userNameCache",key="#userName",unless="#result==null")
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
	
	
	
	
	

	@SuppressWarnings("unused")
	@PostMapping("/swithmobilenumber")
	public ResponseEntity<ResponseObject> swithMobileNumber(
			@RequestParam(value = "oldMobileNumber", required = false) String oldMobileNumber,
			@RequestParam(value = "newMobileNumber", required = false) String newMobileNumber) throws ResourceNotFoundException {

		if (oldMobileNumber == null) {

			response.setError("1");
			response.setMessage("'old mobile number' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");

			return ResponseEntity.ok(response);

		} else if (newMobileNumber == null) {

			response.setError("1");
			response.setMessage("'profileid' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");

			return ResponseEntity.ok(response);

		} else {

			Long mobileNumber = 0l;
			try {
				mobileNumber = Long.parseLong(oldMobileNumber);
			} catch (NumberFormatException e) {

				response.setError("1");
				response.setMessage("wrong MobileNumber please enter numeric value");
				response.setData(empty);
				response.setStatus("FAIL");
				return ResponseEntity.ok(response);

			}

			List<UserRegister> register = null;
			UserRegister updateMobileNumber = null;
			UserOtp userOtp =new UserOtp();
			register = userRegisterRepository.findByMobileNumber(oldMobileNumber);
			if (!register.isEmpty()) {
				
				register.get(0).setMobilNumber(newMobileNumber);
				updateMobileNumber = userRegisterRepository.save(register.get(0));

				response.setMessage("your Display name updated successfully");
				response.setData("Your New Mobile number : "+newMobileNumber);
				response.setError("");
				response.setStatus("success");

				return ResponseEntity.ok(response);
			} else {
				response.setMessage("user not available");
				response.setData(empty);
				response.setError("1");
				response.setStatus("fail");
				return ResponseEntity.ok(response);
			}
		}
	}
	
	@SuppressWarnings({ "unchecked" })
	@PostMapping("/setChatType")
	public ResponseEntity<ResponseObject> createFirebaseChatType(@RequestParam(value ="senderId", required=false) String senderId,
			@RequestParam(value ="receiverId", required=false) String receiverId, 
			@RequestParam(value ="type", required=false) String type) {

		if(senderId == null ) {
			
			response.setError("1");
			response.setMessage("'senderId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
		
		}
		else if (receiverId == null) {

			response.setError("1");
			response.setMessage("'receiverId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);

		}else if (type == null) {
			response.setError("1");
			response.setMessage("'type' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);

		} else {
			
			try {
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials
								.fromStream(new ClassPathResource("/craziapp-3c02b-firebase-adminsdk-rrs6o-3add9ace15.json").getInputStream()))
						.setDatabaseUrl(FB_BASE_URL).build();
				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			
			String hql="FROM UserRegister WHERE userName= ?1";
			List<UserRegister> senderList=entitymanager.createQuery(hql).setParameter(1, senderId).getResultList();
			
			List<UserRegister> receiverList=entitymanager.createQuery(hql).setParameter(1, receiverId).getResultList();
			
			UserRegister senderUser = new UserRegister();
			UserRegister reciverUser = new UserRegister();
			
			if(!senderList.isEmpty()) {
				
				senderUser = senderList.get(0);
			}
			
			if(!receiverList.isEmpty()) {
				
				reciverUser = receiverList.get(0);
			}
			if(senderUser.equals(null) || senderUser == null || reciverUser.equals(null) || reciverUser == null) {
				
				System.out.println("User doesn't exist");
				
			}else {
				
				if(senderUser.getProfile().getCurrentProfile()!= null || senderUser.getProfile().getCurrentProfile()!= "" || reciverUser.getProfile().getCurrentProfile()!= null || reciverUser.getProfile().getCurrentProfile()!= "") {
				
					Map<String, Object> groups = new HashMap<>();
					groups.put(senderId+"_"+receiverId, new ChatSecreteFirebase(senderId,receiverId, type, senderUser.getProfile().getCurrentProfile(),reciverUser.getProfile().getCurrentProfile()));
					database.getReference().child("chatType").updateChildrenAsync(groups);
				
				}else {
					
					Map<String, Object> groups = new HashMap<>();
					groups.put(senderId+"_"+receiverId, new ChatSecreteFirebase(senderId,receiverId, type, senderUser.getProfile().getCurrentProfile(),reciverUser.getProfile().getCurrentProfile()));
					database.getReference().child("chatType").updateChildrenAsync(groups);
					System.out.println("User updated with data profile");
					
				}
			}
		
			//ref.child("group").setValueAsync("group", new GroupFirebase(groupProfile.getGroupId(), groupProfile.getDisplayName(), userRegister.getUserName(), groupProfile.getGroupMember()));
			//ref.getDatabase().getReference().push().c.getReferenceFromUrl(FB_BASE_URL).setValueAsync(groups);
	
			//database.getReference().child("groups").setValueAsync( new GroupFirebase(1, groupProfile.getDisplayName(), userRegister.getUserName(), "+919657070183"));
			//database.getReference().child("groups").push().setValueAsync(new GroupFirebase(groupProfile.getGroupId()));
			//usersRef.setValueAsync(groups);
			
			response.setStatus("Success");
			response.setMessage("Chat Type setted successfully");
			response.setError("0");
			response.setData(empty);

			return ResponseEntity.ok(response);

		}

	}
	
	@SuppressWarnings("unchecked")
	@PostMapping("/updateChatType")
	public ResponseEntity<ResponseObject> updateFirebaseChatType(@RequestParam(value ="senderId", required=false) String senderId,
			@RequestParam(value ="receiverId", required=false) String receiverId, 
			@RequestParam(value ="type", required=false) String type) {

		if(senderId == null ) {
			
			response.setError("1");
			response.setMessage("'senderId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);
		
		}
		else if (receiverId == null) {

			response.setError("1");
			response.setMessage("'receiverId' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);

		}else if (type == null) {
			response.setError("1");
			response.setMessage("'type' is empty or null please check");
			response.setData(empty);
			response.setStatus("FAIL");
			
			return ResponseEntity.ok(response);

		} else {
			
			try {
				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials
								.fromStream(new ClassPathResource("/craziapp-3c02b-firebase-adminsdk-rrs6o-3add9ace15.json").getInputStream()))
						.setDatabaseUrl(FB_BASE_URL).build();
				if (FirebaseApp.getApps().isEmpty()) {
					FirebaseApp.initializeApp(options);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			FirebaseDatabase database = FirebaseDatabase.getInstance();
			

			String hql="FROM UserRegister as ur WHERE ur.userName= ?1";
			List<UserRegister> senderList=entitymanager.createQuery(hql).setParameter(1, senderId).getResultList();
			
			List<UserRegister> receiverList=entitymanager.createQuery(hql).setParameter(1, receiverId).getResultList();
			
			UserRegister senderUser = new UserRegister();
			UserRegister reciverUser = new UserRegister();
			
			if(!senderList.isEmpty()) {
				
				senderUser = senderList.get(0);
			}
			
			if(!receiverList.isEmpty()) {
				
				reciverUser = receiverList.get(0);
			}
			if(senderUser.equals(null) || senderUser == null || reciverUser.equals(null) || reciverUser == null) {
				
				System.out.println("User doesn't exist");
				
			}else {
				
				if(senderUser.getProfile().getCurrentProfile()!= null || senderUser.getProfile().getCurrentProfile()!= "" || reciverUser.getProfile().getCurrentProfile()!= null || reciverUser.getProfile().getCurrentProfile()!= "") {
				
					Map<String, Object> groups = new HashMap<>();
					groups.put(senderId+"_"+receiverId, new ChatSecreteFirebase(senderId,receiverId, type, senderUser.getProfile().getCurrentProfile(),reciverUser.getProfile().getCurrentProfile()));
					database.getReference().child("chatType").updateChildrenAsync(groups);
				
					System.out.println("User updated with data");
				}
				
				else {
					
					Map<String, Object> groups = new HashMap<>();
					groups.put(senderId+"_"+receiverId, new ChatSecreteFirebase(senderId,receiverId, type, senderUser.getProfile().getCurrentProfile(),reciverUser.getProfile().getCurrentProfile()));
					database.getReference().child("chatType").updateChildrenAsync(groups);
					System.out.println("User updated with data profile");
					
				}
			}
		
			
			response.setStatus("Success");
			response.setMessage("Chat Type updated successfully");
			response.setError("0");
			response.setData(empty);

			return ResponseEntity.ok(response);

		}

	}
	


}
