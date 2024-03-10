package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.List;



@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    private AssignmentRepository assignmentRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private GradeRepository gradeRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;


    // instructor lists assignments for a section.  Assignments ordered by due date.
    // logged in user must be the instructor for the section
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(@PathVariable("secNo") int secNo) {

        // TODO DONE remove the following line when done
        // hint: use the assignment repository method
        //  findBySectionNoOrderByDueDate to return
        //  a list of assignments
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);

        List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
        for (Assignment a : assignments) {
            assignmentDTOList.add(new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDate(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo()));
        }
        return assignmentDTOList;
    }

    // add assignment
    // TODO **************user must be instructor of the section************************
    // return AssignmentDTO with assignmentID generated by database
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
        @RequestBody AssignmentDTO assignmentDTO) {
        Assignment a = new Assignment();
        a.setAssignmentId(assignmentDTO.id());
        a.setTitle(assignmentDTO.title());
        a.setDueDate(assignmentDTO.dueDate());

        Section section = sectionRepository.findById(assignmentDTO.secNo()).orElse(null);

        if (section != null) {
            a.setSection(section);
            assignmentRepository.save(a);

            return new AssignmentDTO(
                a.getAssignmentId(),
                a.getTitle(),
                a.getDueDate(),
                a.getSection().getCourse().getCourseId(),
                a.getSection().getSecId(),
                a.getSection().getSectionNo()
            );
        } else {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND);
        }
    }

        // update assignment for a section.  Only title and dueDate may be changed.
        // TODO************user must be instructor of the section *************************
        // return updated AssignmentDTO
        @PutMapping("/assignments")
        public AssignmentDTO updateAssignment (@RequestBody AssignmentDTO dto){
            Assignment u = assignmentRepository.findById(dto.id()).orElse(null);
            if(u == null) {
                throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "Assignment not found "+ dto.id());
            } else {
                u.setTitle(dto.title());
                u.setDueDate(dto.dueDate());
                assignmentRepository.save(u);
                return new AssignmentDTO(
                    u.getAssignmentId(),
                    u.getTitle(),
                    u.getDueDate(),
                    u.getSection().getCourse().getCourseId(),
                    u.getSection().getSecId(),
                    u.getSection().getSectionNo()
                );
            }
        }

        // delete assignment for a section
        // TODO***************logged in user must be instructor of the section*********************
        @DeleteMapping("/assignments/{assignmentId}")
        public void deleteAssignment ( @PathVariable("assignmentId") int assignmentId){
            Assignment d = assignmentRepository.findById(assignmentId).orElse(null);
            if (d != null) {
                assignmentRepository.delete(d);
            }
        }

        // instructor gets grades for assignment ordered by student name
        // user must be instructor for the section
        //
        // TODO remove the following line when done
        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        //   hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        //   if the grade does not exist, create a grade entity and set the score to NULL
        //   and then save the new entity
        @GetMapping("/assignments/{assignmentId}/grades")
        public List<GradeDTO> getAssignmentGrades ( @PathVariable("assignmentId") int assignmentId){

//        int sectionNo = assignmentRepository.findSectionNoByAssignmentId(assignmentId);
            Assignment a = assignmentRepository.findById(assignmentId).orElse(null);
            if(a==null){
                throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "not found");


            }

            List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(a.getSection().getSectionNo());
            List<GradeDTO> assignmentGrades = new ArrayList<>();

            for (Enrollment enrollment : enrollments) {
                Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignmentId);

                if (grade == null) {
//                    grade = new Grade();
//                    grade.setGradeId(grade.getGradeId());
//                    grade.setScore(null);
//                    grade.setGradeId(grade.getGradeId());
//                    grade.setAssignment(grade.getAssignment());
//                    gradeRepository.save(grade);
                    return null;
                }


                    GradeDTO gradeDTO = new GradeDTO(
                        grade.getGradeId(),
                        enrollment.getStudent().getName(),
                        enrollment.getStudent().getEmail(),
                        grade.getAssignment().getTitle(),
                        grade.getAssignment().getSection().getCourse().getCourseId(),
                        grade.getAssignment().getSection().getSecId(),
                        grade.getScore());
                    assignmentGrades.add(gradeDTO);
            }
            return assignmentGrades;
        }

        // instructor uploads grades for assignment
        // user must be instructor for the section

        @PutMapping("/grades")
        public void updateGrades (@RequestBody List <GradeDTO> dlist) {
            // TODO
            // for each grade in the GradeDTO list, retrieve the grade entity
            // update the score and save the entity
            for (GradeDTO dto : dlist) {
                Grade g = gradeRepository.findById(dto.gradeId()).orElse(null);
                if (g == null) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "dto not found " + dto.courseId());
                } else {
                    g.setScore(dto.score());
                    gradeRepository.save(g);
//                    return new GradeDTO(
//                            g.getGradeId(),
//                            g.getScore(),
//                            g.getAssignment(),
//                            g.getAssignment()
                }
            }
        }

        // student lists their assignments/grades for an enrollment ordered by due date
        // student must be enrolled in the section
        @GetMapping("/assignments")
        public List<AssignmentDTO> getStudentAssignments (
        @RequestParam("studentId") int studentId,
        @RequestParam("year") int year,
        @RequestParam("semester") String semester){

            // TODO return a list of assignments and (if they exist) the assignment grade*****
            // for all sections that the student is enrolled for the given year and semester
            // hint: use the assignment repository method findByStudentIdAndYearAndSemesterOrderByDueDate

            List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId,year,semester);

            List<AssignmentDTO> assignmentDTOList = new ArrayList<>();
            for(Assignment a: assignments){
                assignmentDTOList.add(new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()));
            }
            return assignmentDTOList;
        }
    }