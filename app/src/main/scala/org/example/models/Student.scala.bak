package org.example.models

import org.springframework.data.cassandra.core.mapping.{PrimaryKey, Table}
import scala.beans.BeanProperty

@Table("students")
class Student {
  
  @PrimaryKey
  @BeanProperty
  var studentId: Int = _
  
  @BeanProperty
  var name: String = _
  
  @BeanProperty
  var email: String = _
  
  @BeanProperty
  var program: String = _
  
  @BeanProperty
  var enrolmentYear: Int = _
  
  @BeanProperty
  var interests: String = _
  
  def this(studentId: Int, name: String, email: String, program: String, 
           enrolmentYear: Int, interests: String) = {
    this()
    this.studentId = studentId
    this.name = name
    this.email = email
    this.program = program
    this.enrolmentYear = enrolmentYear
    this.interests = interests
  }
}