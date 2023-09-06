package com.cst438;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;

import java.sql.Date;

/*
 * This example shows how to use selenium testing using the web driver 
 * with Chrome browser.
 * 
 *  - Buttons, input, and anchor elements are located using XPATH expression.
 *  - onClick( ) method is used with buttons and anchor tags.
 *  - Input fields are located and sendKeys( ) method is used to enter test data.
 *  - Spring Boot JPA is used to initialize, verify and reset the database before
 *      and after testing.
 *      
 *  In SpringBootTest environment, the test program may use Spring repositories to 
 *  setup the database for the test and to verify the result.
 */

@SpringBootTest
public class EndToEndTestAddAssignment {

   public static final String CHROME_DRIVER_FILE_LOCATION = "C:/chromedriver_win32/chromedriver.exe";

   public static final String URL = "http://localhost:3000";
   public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
   public static final String TEST_COURSE_TITLE = "Tester Course";
   public static final int TEST_COURSE_ID = 99999;
   public static final int SLEEP_DURATION = 1000; // 1 second.
   public static final String TEST_ASSIGNMENT_NAME = "Tester Assignment 542";
   public static final String TEST_ASSIGNMENT_DUE_DATE = "2023-03-30";
   public static final String TEST_ASSIGNMENT_COURSE_ID = "99999";
   

   @Autowired
   CourseRepository courseRepository;

   @Autowired
   AssignmentRepository assignmentRepository;

   @Test
   public void addCourseTest() throws Exception {

      //              Database setup:  create course          
      Course c = new Course();
      c.setCourse_id(TEST_COURSE_ID);
      c.setInstructor(TEST_INSTRUCTOR_EMAIL);
      c.setSemester("Fall");
      c.setYear(2021);
      c.setTitle(TEST_COURSE_TITLE);      
      
      Assignment a = null;

      courseRepository.save(c);
      // a = assignmentRepository.save(a);

      System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_FILE_LOCATION);
      WebDriver driver = new ChromeDriver();
      // Puts an Implicit wait for 10 seconds before throwing exception
      driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

      driver.get(URL);
      Thread.sleep(SLEEP_DURATION);


      try
      {
         WebElement we = driver.findElement(By.id("addAssignmentButton"));
         we.click();
         Thread.sleep(SLEEP_DURATION);
         
         driver.findElement(By.id("addAssignmentName")).sendKeys(TEST_ASSIGNMENT_NAME);
         driver.findElement(By.id("addAssignmentDueDate")).sendKeys(TEST_ASSIGNMENT_DUE_DATE);
         driver.findElement(By.id("addAssignmentCourseId")).sendKeys(TEST_ASSIGNMENT_COURSE_ID);
         
         we = driver.findElement(By.id("Add"));
         we.click();
         Thread.sleep(SLEEP_DURATION);
         
         List<Assignment> assignments = assignmentRepository.findAssignmentsByName(TEST_ASSIGNMENT_NAME);
         a = new Assignment();
         a.setCourse(assignments.get(0).getCourse());
         // set assignment due date to 24 hours ago
         a.setDueDate(assignments.get(0).getDueDate());
         a.setName(assignments.get(0).getName());
         a.setNeedsGrading(assignments.get(0).getNeedsGrading());
         a.setId(assignments.get(0).getId());
         
         assertEquals(TEST_ASSIGNMENT_NAME, a.getName(), "Assignment Name does not match.");
         assertEquals(Date.valueOf(TEST_ASSIGNMENT_DUE_DATE), a.getDueDate(), "Assignment Due Date does not match.");
         assertEquals(TEST_COURSE_ID, a.getCourse().getCourse_id(), "Assignment Course ID does not match.");

      } catch (Exception ex) {
         throw ex;
      } finally {

         /*
          *  clean up database so the test is repeatable.
          */
         try {
            assignmentRepository.delete(a);
            courseRepository.delete(c);
         }
         catch (Exception ex) {
            System.out.println("assignment or course was null in database search, could not be deleted");
            throw ex;
         }

         driver.quit();
      }

   }
}
