package com.digitalbooks.user.controller;

import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.digitalbooks.user.dto.Books;
import com.digitalbooks.user.dto.BooksWithByteFile;
import com.digitalbooks.user.dto.BooksWithLogo;
import com.digitalbooks.user.dto.BooksWithMultipartFile;
import com.digitalbooks.user.jwt.AuthRequest;
import com.digitalbooks.user.jwt.JwtResponse;
import com.digitalbooks.user.jwt.JwtUtil;
import com.digitalbooks.user.jwt.UserDetailsImpl;
import com.digitalbooks.user.model.Subscription;
import com.digitalbooks.user.model.Users;
import com.digitalbooks.user.payload.response.MessageResponse;
import com.digitalbooks.user.service.UserService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/digitalbooks")
public class UserServiceController {

	@Value("${spring.datasource.url}")
	 private  String server;
	
	MultipartFile file;
	byte[] byteslogo;
	
	@Autowired
	private JwtUtil jwtUtil;
	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private UserService userService;
	
	String bookUrl = "http://localhost:8082/digitalbooks/searchBook/";
	String createBook= "http://localhost:8082/digitalbooks/author/";
	
	
	public static final String USER_NOT_FOUND = "User is not found";
	public static final String BOOK_NOT_FOUND = "No book is available for current selection";
	public static final String SUBSCRIPTION_NOT_FOUND ="Subscription is not found";
	
	@GetMapping("/test")
	public String test() {
		System.out.println("Server:c"+server);

		return "It is User Service "+server ;
			}

	
	@PostMapping(value="/uploadlogo")
	public void uploadLogo(@RequestParam("file") MultipartFile file) throws IOException {
		//this.file=file;	
		try {
			//BooksWithMultipartFile book =(BooksWithMultipartFile) bookLogo;
			if (file != null) {
				this.byteslogo = file.getBytes();
		         
			}
		} catch (Exception e) {
		//	e.printStackTrace();
			throw new IOException(e.getMessage()); ///sonar
		} 
		
		
	}
	@PostMapping(value="/author/{author-id}/books", consumes = { "application/json" })
    public ResponseEntity<?> saveBook(
    		//@RequestParam(value="file", required=false) MultipartFile file,
    		@RequestBody Books book, 
    		@PathVariable("author-id") int authorId) throws IOException {
		
		boolean isUserAuthor = userService.checkAuthorExists(authorId);
		if(isUserAuthor) {
			byte[] bytes = this.byteslogo;
			
//			try {
//				//BooksWithMultipartFile book =(BooksWithMultipartFile) bookLogo;
//				if (this.file != null) {
//					bytes = this.file.getBytes();
//			         
//				}
//			} catch (Exception e) {
//			//	e.printStackTrace();
//				throw new IOException(e.getMessage()); ///sonar
//			} 
			BooksWithByteFile booksWithLogo = new BooksWithByteFile();
			booksWithLogo.setFile(bytes);
			booksWithLogo.setBooks(book);

			Books savedBook = restTemplate.postForObject(createBook+authorId, booksWithLogo, Books.class);

			if(savedBook!=null) {
				return ResponseEntity.ok(savedBook);	
			}
			else {
		       return ResponseEntity.badRequest().body(new MessageResponse("Author has already a book with same Name"));
			}
			
		}
		else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User is either not present or user does not have author role"));
		}
			
	}
		
	@PostMapping("/author/{author-id}/books/{book-id}")
	public ResponseEntity<?> updateBook(
    		@RequestBody Books book,
    		@PathVariable("author-id") int authorId,
    		@PathVariable("book-id") int bookId) throws Exception {
		boolean isUserAuthor = userService.checkAuthorExists(authorId);
		if(isUserAuthor) {
			byte[] bytes = this.byteslogo;
		//	byte[] bytes = null;
//			try {
//				
//				if (file != null) {
//					bytes = file.getBytes();
//			         
//				}
//			} catch (IOException e) {
//				throw new IOException(e.getMessage()); ///sonar
//			} 
			BooksWithByteFile booksWithLogo = new BooksWithByteFile();
			booksWithLogo.setFile(bytes);
			booksWithLogo.setBooks(book);
			Books updatedBook = restTemplate.postForObject(createBook+authorId+"/"+bookId, booksWithLogo, Books.class);
			
			if(updatedBook!=null) {
				return ResponseEntity.ok(updatedBook);		
			}
			else {
		       return ResponseEntity.badRequest().body(new MessageResponse("Author does not have the book"));
			}
		}
		else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("User is either not present or user does not have author role"));
		}
		
		
		
	}
		

	@PostMapping("/sign-in")
	public ResponseEntity<?> generateToken(@RequestBody AuthRequest authRequest,
			HttpServletResponse httpServletResponse, HttpSession session) throws Exception {
		try {
			System.out.println(authRequest.getUserName() + " and "+ authRequest.getPassword());
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(authRequest.getUserName(), authRequest.getPassword()));

			SecurityContextHolder.getContext().setAuthentication(authentication);
			String jwt = jwtUtil.generateToken(authRequest.getUserName());
			UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
			List<String> roles = userDetails.getAuthorities().stream().map(item -> item.getAuthority())
					.collect(Collectors.toList());

			return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
					userDetails.getEmail(), roles));
		} catch (Exception ex) {
			return ResponseEntity.badRequest().body(new MessageResponse("Invalid username or password!"));
			
		}

	}

	@PostMapping("/sign-up")
	public ResponseEntity<?> signUp(@RequestBody Users user) {
		Users duplicateUser = userService.duplicateUserNameAnsEmail(user.getUserName(), user.getEmail());
		if (duplicateUser != null) {
			return ResponseEntity.badRequest()
					.body(new MessageResponse("Username and Email combination is already taken!"));
		}
		user.setPassword(encoder.encode(user.getPassword()));
		user.setCreatedDate(new Date());
		userService.saveUser(user);
		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));

	}

	

	@GetMapping("/searchBook")
	public ResponseEntity<?> searchBook(@RequestParam("category") String category, @RequestParam("title") String title,
			@RequestParam("author") String author, @RequestParam("price") String price,
			@RequestParam("publisher") String publisher) throws Exception {

		Books responseBook = new Books();
		BooksWithLogo booksWithLogo = null;
		int authoId = userService.findByUserName(author);
		byte[] logo = restTemplate.getForObject(
				bookUrl + "logo/" + category + "/" + title + "/" + authoId + "/" + price + "/" + publisher,
				byte[].class);
		responseBook = restTemplate.getForObject(
				bookUrl + category + "/" + title + "/" + authoId + "/" + price + "/" + publisher, Books.class);
		try {
			Blob blob = userService.fetchBlob(logo);
			booksWithLogo = new BooksWithLogo(blob, responseBook);
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		if (responseBook == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("No book is available for current selection"));
		} else {
			return ResponseEntity.ok(booksWithLogo);
		}

	}
	
	@PutMapping("author/{author-id}/books/{book-id}")
	public ResponseEntity<?> blockBook(@PathVariable("author-id") int userId, @PathVariable("book-id") int bookId, @RequestParam("block") String block) {
	 return restTemplate.getForEntity(bookUrl+"cancel/"+userId+"/"+bookId+"/"+block , MessageResponse.class);		
	}
	
	/**fetch book for update
	 * @throws Exception **/
	@GetMapping("book/{book-id}")
	public ResponseEntity<?> fetchBookForUpdate(@PathVariable("book-id") int bookId) throws Exception {
		Books responseBook = new Books();
		BooksWithLogo booksWithLogo = null;
		byte[] logo = restTemplate.getForObject(
				bookUrl+"/logo/"+bookId,
				byte[].class);
		responseBook = restTemplate.getForObject(
				bookUrl+bookId, Books.class);
		try {
			Blob blob = userService.fetchBlob(logo);
			booksWithLogo = new BooksWithLogo(blob, responseBook);
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
		if (responseBook == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No book is available for current selection");
		} else {
			return ResponseEntity.ok(booksWithLogo);
		}
	 //return restTemplate.getForEntity(bookUrl+bookId, BooksWithLogo.class);		
	}
	
	
	
	/**Get all createdbook of an user for front end
	 * @throws Exception **/
	@GetMapping("createdbook/{user-id}")
	public ResponseEntity<?> getSubscriptionId(@PathVariable("user-id") int user) throws Exception {

		Books[] responseBook = null;
		BooksWithLogo booksWithLogo = null;

		boolean userId = userService.checkAuthorExists(user);
		if (!userId) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(USER_NOT_FOUND);
		} else {
			//Optional<List<Subscription>> subscriptionList = userService.fetchSubscribedBooksForUser(userId);
			List<BooksWithLogo> listofBooks = new ArrayList<>();
			//int count= restTemplate.getForObject(bookUrl + "count/book"+user , Integer.class);
		//	if (!subscriptionList.isEmpty() && !subscriptionList.get().isEmpty()) {
				//for (int i = 0; i < count; i++) {
				//	int bookId = subscriptionList.get().get(i).getBookId();

					byte[] logo[] = restTemplate.getForObject(bookUrl + "createdbook/logo/" + user, byte[][].class);
					responseBook = restTemplate.getForObject(bookUrl + "createdbook/" + user, Books[].class);

					try {
						for(int i=0 ;i <responseBook.length;i++) {
							Blob blob = userService.fetchBlob(logo[i]);
							booksWithLogo = new BooksWithLogo(blob, responseBook[i]);
							listofBooks.add(booksWithLogo);
						}
						
					} catch (Exception ex) {
						throw new Exception(ex.getMessage());
					}
					if (responseBook == null) {
						return ResponseEntity.status(HttpStatus.NOT_FOUND).body(BOOK_NOT_FOUND);
					}
			//	}

				return ResponseEntity.ok(listofBooks);
			//}

//			else {
//				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No subscription found");
//			}

		}

	
	}

}
