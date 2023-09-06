package com.cst438.controllers;

import java.util.ArrayList;
import java.util.List;
import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentListDTO;
import com.cst438.domain.AssignmentGrade;
import com.cst438.domain.AssignmentGradeRepository;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Course;
import com.cst438.domain.CourseDTOG;
import com.cst438.domain.CourseRepository;
import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentDTO;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.GradebookDTO;
import com.cst438.services.RegistrationService;

@RestController
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
public class AssignmentController
{
   @Autowired
   AssignmentRepository assignmentRepository;
   
   @Autowired
   CourseRepository courseRepository;
   
   //As an instructor for a course , I can add a new assignment for my course.  The assignment has a name and a due date.
   @PostMapping("/assignment")
   @Transactional
   public AssignmentListDTO.AssignmentDTO addAssignment(@RequestBody AssignmentListDTO.AssignmentDTO AssignmentDTO)
   {
      String instructorEmail = "dwisneski@csumb.edu";
      
      String assignmentName = AssignmentDTO.assignmentName;
      String dateStr = AssignmentDTO.dueDate;
      Date assignment_due_date = Date.valueOf(dateStr);
      
      //course must already exist
      Course course  = courseRepository.findById(AssignmentDTO.courseId).orElse(null);
      
      if (assignmentName != null && assignment_due_date != null && course != null)
      {
         Assignment assignment = new Assignment();
         assignment.setCourse(course);
         assignment.setName(assignmentName);
         assignment.setDueDate(assignment_due_date);
         assignment.setNeedsGrading(1); //default to still needs to be graded
         
         Assignment savedAssignment =  assignmentRepository.save(assignment);
         
         //any information need to be sent to registrationService?
         
         AssignmentListDTO.AssignmentDTO result = createAssignmentDTO(savedAssignment);
         return result;
      }
      else
      {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Course_id invalid or assignment name or due date not found.  " + AssignmentDTO.courseId);
      }
   }
   
   // As an instructor, I can change the name of the assignment for my course.
   @PutMapping("/assignment/{id}")
   @Transactional
   public void changeAssignmentName(@RequestBody AssignmentListDTO.AssignmentDTO AssignmentDTO,
         @PathVariable("id") Integer assignmentId)
   {
      String instructorEmail = "dwisneski@csumb.edu";
      
      Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
      if (assignment == null) {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Invalid assignmentID. " + assignmentId);
      }
      if (!assignment.getCourse().getInstructor().equals(instructorEmail)) {
         throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Wrong instructor. " + assignmentId);
      }
      
      assignment.setName(AssignmentDTO.assignmentName);
      assignmentRepository.save(assignment);
   }
   
   //As an instructor, I can delete an assignment  for my course (only if there are no grades for the assignment).
   @DeleteMapping("/assignment/{assignmentId}")
   @Transactional
   public void deleteAssignment(@PathVariable int assignmentId)
   {
      String instructorEmail = "dwisneski@csumb.edu";

      Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
      
      if (assignment.getAssignmentGrades().size() > 0)
      {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cannot delete: assignment contains grades  " + assignmentId);
      }

      //verify that assignment exists and
      if (assignment != null && assignment.getCourse().getInstructor().equals(instructorEmail))
      {
         assignmentRepository.delete(assignment);
      } else
      {
         throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assignmentId invalid or wrong instructor. " + assignmentId);
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

