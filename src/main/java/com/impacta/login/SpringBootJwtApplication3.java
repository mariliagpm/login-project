package com.impacta.login;

import org.springframework.security.crypto.bcrypt.BCrypt;

public class SpringBootJwtApplication3 {

	  
	 public static void main(String args[]){
		 
		 String a  = "teste";
		String		 senhabanco = "$2a$10$.H/j7Lzgq589jRCQ2dAJxOXPmY7ZaeFOzGqUucExPWCmduNc06oK.";
			boolean samePass = BCrypt.checkpw(a,senhabanco);
			System.out.println(" " +samePass );
				  
	 }
 

	 
	
}
