package org.example.controllers

import org.example.models.Student
import org.example.repositories.StudentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{HttpStatus, ResponseEntity}
import org.springframework.web.bind.annotation._

import scala.jdk.CollectionConverters._

@RestController
@RequestMapping(Array("/api/students"))
@CrossOrigin(origins = Array("http://localhost:3000")) // Allow React frontend
class StudentController @Autowired()(studentRepository: StudentRepository) {

  @GetMapping(Array(""))
  def getAllStudents(): ResponseEntity[java.util.List[Student]] = {
    val students = studentRepository.findAll()
    ResponseEntity.ok(students)
  }

  @GetMapping(Array("/{id}"))
  def getStudentById(@PathVariable id: Int): ResponseEntity[Student] = {
    val student = studentRepository.findById(id)
    if (student.isPresent) {
      ResponseEntity.ok(student.get())
    } else {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @PostMapping(Array(""))
  def createStudent(@RequestBody student: Student): ResponseEntity[Student] = {
    val savedStudent = studentRepository.save(student)
    ResponseEntity.status(HttpStatus.CREATED).body(savedStudent)
  }

  @PutMapping(Array("/{id}"))
  def updateStudent(@PathVariable id: Int, 
                   @RequestBody studentDetails: Student): ResponseEntity[Student] = {
    val studentOpt = studentRepository.findById(id)
    
    if (studentOpt.isPresent) {
      val student = studentOpt.get()
      student.name = studentDetails.name
      student.email = studentDetails.email
      student.program = studentDetails.program
      student.enrolmentYear = studentDetails.enrolmentYear
      student.interests = studentDetails.interests
      
      val updatedStudent = studentRepository.save(student)
      ResponseEntity.ok(updatedStudent)
    } else {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @DeleteMapping(Array("/{id}"))
  def deleteStudent(@PathVariable id: Int): ResponseEntity[Void] = {
    if (studentRepository.existsById(id)) {
      studentRepository.deleteById(id)
      ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    } else {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }

  @GetMapping(Array("/email/{email}"))
  def getStudentByEmail(@PathVariable email: String): ResponseEntity[Student] = {
    val student = studentRepository.findByEmail(email)
    if (student.isPresent) {
      ResponseEntity.ok(student.get())
    } else {
      ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }
  }
}