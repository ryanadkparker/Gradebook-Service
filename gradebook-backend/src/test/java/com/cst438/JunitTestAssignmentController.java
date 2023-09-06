package com.cst438;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import com.cst438.controllers.AssignmentController;
import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.GradebookDTO;
import com.cst438.services.RegistrationService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = { AssignmentController.class })
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest
public class JunitTestAssignmentController
{
   static final String URL = "http://localhost:8080";
   public static final int TEST_COURSE_ID = 40442;
   public static final String TEST_STUDENT_EMAIL = "test@csumb.edu";
   public static final String TEST_STUDENT_NAME = "test";
   public static final String TEST_INSTRUCTOR_EMAIL = "dwisneski@csumb.edu";
   public static final int TEST_YEAR = 2023;
   public static final String TEST_SEMESTER = "Spring";
   
   Course course = null;
   Enrollment enrollment = null;
   Assignment assignment = null;
   AssignmentGrade ag = null;

   @MockBean
   AssignmentRepository assignmentRepository;

   @MockBean
   AssignmentGradeRepository assignmentGradeRepository;

   @MockBean
   CourseRepository courseRepository;

   @MockBean
   RegistrationService registrationService;

   @Autowired
   private MockMvc mvc;
   
   @BeforeEach
   void init()
   {
      // mock database data
      course = new Course();
      course.setCourse_id(TEST_COURSE_ID);
      course.setSemester(TEST_SEMESTER);
      course.setYear(TEST_YEAR);
      course.setInstructor(TEST_INSTRUCTOR_EMAIL);
      course.setEnrollments(new java.util.ArrayList<Enrollment>());
      course.setAssignments(new java.util.ArrayList<Assignment>());

      enrollment = new Enrollment();
      enrollment.setCourse(course);
      course.getEnrollments().add(enrollment);
      enrollment.setId(TEST_COURSE_ID);
      enrollment.setStudentEmail(TEST_STUDENT_EMAIL);
      enrollment.setStudentName(TEST_STUDENT_NAME);

      assignment = new Assignment();
      assignment.setCourse(course);
      course.getAssignments().add(assignment);
      // set dueDate to 1 week before now.
      assignment.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
      assignment.setId(1);
      assignment.setName("Assignment 1");
      assignment.setNeedsGrading(1);

      ag = new AssignmentGrade();
      ag.setAssignment(assignment);
      ag.setId(1);
      ag.setScore("");
      ag.setStudentEnrollment(enrollment);

      // given -- stubs for database repositories that return test data
      given(assignmentRepository.findById(1)).willReturn(Optional.of(assignment));
      given(assignmentGradeRepository.findByAssignmentIdAndStudentEmail(1, TEST_STUDENT_EMAIL)).willReturn(null);
      given(assignmentGradeRepository.save(any())).willReturn(ag);
      // end of mock data
   }

   @Test
   public void testAddAssignment()
   {
      MockHttpServletResponse response = null;
      
      try
      {
         response = mvc.perform(MockMvcRequestBuilders
               .post("/assignment")
               .content(asJsonString(createAssignmentDTO(assignment)))
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON))
             .andReturn().getResponse();
      } catch (Exception e1)
      {
         e1.printStackTrace();
      }
      
      assertEquals(200, response.getStatus());
      
      AssignmentListDTO.AssignmentDTO result = null;
      try
      {
         result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      assertNotEquals(0, result.assignmentId);

      verify(assignmentRepository, times(1)).save(any());
   }

   @Test
   public void testChangeAssignmentName()
   {
      MockHttpServletResponse response = null;
      
      Assignment assignment2 = new Assignment();
      assignment2.setCourse(course);
      assignment2.setDueDate(new java.sql.Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000));
      assignment2.setId(1);
      assignment2.setName("Assignment 1 - Updated Name");
      assignment2.setNeedsGrading(1);
      
      try
      {
         response = mvc.perform(MockMvcRequestBuilders
               .put("/assignment/1")
               .content(asJsonString(createAssignmentDTO(assignment2)))
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON))
             .andReturn().getResponse();
      } catch (Exception e1)
      {
         e1.printStackTrace();
      }
      
      assertEquals(200, response.getStatus());
      
      AssignmentListDTO.AssignmentDTO result = null;
      try
      {
         result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      assertNotEquals(0, result.assignmentId);
      assertEquals("Assignment 1 - Updated Name", result.assignmentName);

      verify(assignmentRepository, times(1)).save(any());
   }

   @Test
   public void testDeleteAssignment()
   {
      MockHttpServletResponse response = null;
      
      //first add the assignment, then delete it
      try
      {
         response = mvc.perform(MockMvcRequestBuilders
               .post("/assignment")
               .content(asJsonString(createAssignmentDTO(assignment)))
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON))
             .andReturn().getResponse();
      } catch (Exception e1)
      {
         e1.printStackTrace();
      }
      
      assertEquals(200, response.getStatus());
      
      AssignmentListDTO.AssignmentDTO result = null;
      try
      {
         result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      assertNotEquals(0, result.assignmentId);

      verify(assignmentRepository, times(1)).save(any());
      
      //DELETION of assignment
      try
      {
         response = mvc.perform(MockMvcRequestBuilders
               .delete("/assignment/1")
               .content(asJsonString(createAssignmentDTO(assignment)))
               .contentType(MediaType.APPLICATION_JSON)
               .accept(MediaType.APPLICATION_JSON))
             .andReturn().getResponse();
      } catch (Exception e1)
      {
         e1.printStackTrace();
      }
      
      assertEquals(200, response.getStatus());
      
      result = null;
      try
      {
         result = fromJsonString(response.getContentAsString(), AssignmentListDTO.AssignmentDTO.class);
      } catch (UnsupportedEncodingException e)
      {
         e.printStackTrace();
      }
      assertNull(result);
   }

   private static String asJsonString(final Object obj) {
      try {

         return new ObjectMapper().writeValueAsString(obj);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }

   private static <T> T fromJsonString(String str, Class<T> valueType) {
      try {
         return new ObjectMapper().readValue(str, valueType);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   
   private AssignmentListDTO.AssignmentDTO createAssignmentDTO(Assignment a)
   {
      AssignmentListDTO.AssignmentDTO AssignmentDTO = new AssignmentListDTO.AssignmentDTO(
               a.getId(), 
               a.getCourse().getCourse_id(), 
               a.getName(), 
               a.getDueDate().toString(), 
               a.getCourse().getTitle());

      return AssignmentDTO;
   }
}
