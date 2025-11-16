package org.example.config

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class CassandraSchemaInitializer @Autowired()(session: CqlSession) extends ApplicationRunner {

  override def run(args: ApplicationArguments): Unit = {
    println("Initializing Cassandra schema...")
    
    // Create Students table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS students (
          student_id INT PRIMARY KEY,
          name TEXT,
          email TEXT,
          program TEXT,
          enrolment_year INT,
          interests TEXT
      )
      """
    )
    
    // Create Courses table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS courses (
          course_id INT PRIMARY KEY,
          course_code TEXT,
          course_name TEXT,
          credits INT,
          description TEXT
      )
      """
    )
    
    // Create Competencies table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS competencies (
          competency_id INT PRIMARY KEY,
          name TEXT,
          description TEXT,
          category TEXT
      )
      """
    )
    
    // Create Capstone Projects table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS capstone_projects (
          project_id INT PRIMARY KEY,
          title TEXT,
          description TEXT,
          difficulty_level TEXT,
          required_competencies TEXT,
          supervisor TEXT,
          status TEXT
      )
      """
    )
    
    // Create Enrolments table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS enrolments (
          enrolment_id INT PRIMARY KEY,
          student_id INT,
          course_id INT,
          semester TEXT,
          year INT,
          status TEXT,
          final_grade DECIMAL
      )
      """
    )
    
    // Create Assessments table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS assessments (
          assessment_id INT PRIMARY KEY,
          course_id INT,
          name TEXT,
          type TEXT,
          max_score DECIMAL,
          weight DECIMAL,
          due_date DATE
      )
      """
    )
    
    // Create Student Assessments table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS student_assessments (
          student_assessment_id INT PRIMARY KEY,
          enrolment_id INT,
          assessment_id INT,
          score DECIMAL,
          feedback TEXT,
          submitted_at TIMESTAMP
      )
      """
    )
    
    // Create Recommendations table
    session.execute(
      """
      CREATE TABLE IF NOT EXISTS student_project_recommendations (
          recommendation_id INT PRIMARY KEY,
          student_id INT,
          project_id INT,
          score DECIMAL,
          reason TEXT,
          generated_at TIMESTAMP,
          is_accepted BOOLEAN
      )
      """
    )
    
    // Create indexes
    try {
      session.execute("CREATE INDEX IF NOT EXISTS ON students (email)")
      session.execute("CREATE INDEX IF NOT EXISTS ON enrolments (student_id)")
      session.execute("CREATE INDEX IF NOT EXISTS ON capstone_projects (status)")
    } catch {
      case e: Exception => println(s"Index creation note: ${e.getMessage}")
    }
    
    println("Cassandra schema initialization complete!")
  }
}