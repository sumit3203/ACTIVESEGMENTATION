package test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonTester {

	
	
   public static void main(String args[]){
      ObjectMapper mapper = new ObjectMapper();
      try {
         Student student = new Student(1,null);       
         String jsonString = mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(student);
         
         System.out.println("\\\\\\\\");
         System.out.println(jsonString);
         
         Student student2 = new Student(1,"John Doe");       
         jsonString = mapper
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(student2);
         
         System.out.println("\\\\\\\\");
         System.out.println(jsonString);
         
      }
      catch (IOException e) { 
         e.printStackTrace();
      }     
   }
}


@JsonInclude(JsonInclude.Include.NON_NULL)
class Student { 

   // the fields must be public for the annotation mechanism to work
   public int id; 
   public String name;
   
   private SimpleDateFormat df=new SimpleDateFormat();
  
   @JsonProperty(value="description")
   protected String projectDescription="test";

   Student(int id, String name){
      this.id = id;
      this.name = name;
   }   
   
   // needs getter for the private fields
   
   private int uint=-1;
   public int getUint() {
	   return this.uint;
   }
   
   // the name for the JSON export may be defined as below
   @JsonProperty(value="classes")
   private int uclasses=4;
   
   public int getUclasses() {
	   return this.uclasses;
   }
   
   private Date date=new Date("01/01/2020");
   
   public String getDate() {
	   
	   return df.format(date);
   }
  
}