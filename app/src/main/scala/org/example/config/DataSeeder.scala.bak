package org.example.config

import com.datastax.oss.driver.api.core.CqlSession
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataSeeder @Autowired()(session: CqlSession) extends CommandLineRunner {

  override def run(args: String*): Unit = {
    println("Seeding database with sample data...")
    
    // Check if data already exists
    val count = session.execute("SELECT COUNT(*) FROM students").one().getLong(0)
    
    if (count == 0) {
      seedStudents()
      seedCourses()
      seedCompetencies()
      seedCapstoneProjects()
      println("Database seeding complete!")
    } else {
      println("Data already exists, skipping seeding.")
    }
  }
  
  private def seedStudents(): Unit = {
    val students = Seq(
      (1, "Alice Wong", "alice.wong@sunway.edu.my", "Computer Science", 2022, "Artificial Intelligence, Web Development"),
      (2, "Bob Tan", "bob.tan@sunway.edu.my", "Computer Science", 2022, "Mobile Development, Cloud Computing"),
      (3, "Charlie Lee", "charlie.lee@sunway.edu.my", "Computer Science", 2021, "Machine Learning, Data Science"),
      (4, "Diana Chen", "diana.chen@sunway.edu.my", "Information Technology", 2022, "Cybersecurity, Networks"),
      (5, "Eric Lim", "eric.lim@sunway.edu.my", "Computer Science", 2023, "Game Development, Graphics")
    )
    
    students.foreach { case (id, name, email, program, year, interests) =>
      session.execute(
        s"""
        INSERT INTO students (student_id, name, email, program, enrolment_year, interests)
        VALUES ($id, '$name', '$email', '$program', $year, '$interests')
        """
      )
    }
    println("✓ Seeded 5 students")
  }
  
  private def seedCourses(): Unit = {
    val courses = Seq(
      (1, "CS101", "Introduction to Programming", 3, "Fundamentals of programming using Python"),
      (2, "CS201", "Data Structures and Algorithms", 4, "Core data structures and algorithmic thinking"),
      (3, "CS301", "Artificial Intelligence", 3, "Machine learning, neural networks, and AI applications"),
      (4, "CS302", "Web Development", 3, "Full-stack web development with modern frameworks"),
      (5, "CS303", "Mobile App Development", 3, "iOS and Android application development"),
      (6, "CS401", "Machine Learning", 4, "Advanced ML algorithms and deep learning"),
      (7, "CS402", "Cloud Computing", 3, "Cloud architecture, AWS, Azure, deployment"),
      (8, "IT201", "Cybersecurity Fundamentals", 3, "Network security, cryptography, ethical hacking")
    )
    
    courses.foreach { case (id, code, name, credits, desc) =>
      session.execute(
        s"""
        INSERT INTO courses (course_id, course_code, course_name, credits, description)
        VALUES ($id, '$code', '$name', $credits, '$desc')
        """
      )
    }
    println("✓ Seeded 8 courses")
  }
  
  private def seedCompetencies(): Unit = {
    val competencies = Seq(
      (1, "Programming", "Ability to write clean, efficient code", "Technical"),
      (2, "Artificial Intelligence", "Understanding of AI/ML concepts and implementation", "Technical"),
      (3, "Web Development", "Full-stack web application development skills", "Technical"),
      (4, "Mobile Development", "iOS/Android app development", "Technical"),
      (5, "Data Science", "Data analysis, visualization, and statistical modeling", "Technical"),
      (6, "Cloud Computing", "Cloud platform deployment and architecture", "Technical"),
      (7, "Cybersecurity", "Security principles and practices", "Technical")
    )
    
    competencies.foreach { case (id, name, desc, category) =>
      session.execute(
        s"""
        INSERT INTO competencies (competency_id, name, description, category)
        VALUES ($id, '$name', '$desc', '$category')
        """
      )
    }
    println("✓ Seeded 7 competencies")
  }
  
  private def seedCapstoneProjects(): Unit = {
    val projects = Seq(
      (1, "AI-Powered Chatbot for Student Support", 
       "Develop an intelligent chatbot using NLP to answer student queries", 
       "Intermediate", "[2,5]", "Dr. Chin", "Available"),
      
      (2, "E-Commerce Platform with Recommendation Engine", 
       "Build a full-stack e-commerce site with ML-based product recommendations", 
       "Advanced", "[2,3,6]", "Dr. Wong", "Available"),
      
      (3, "Mobile Fitness Tracking App", 
       "Create a cross-platform fitness app with health analytics", 
       "Intermediate", "[4,5]", "Dr. Tan", "Available"),
      
      (4, "Cybersecurity Threat Detection System", 
       "Implement an ML-based system to detect network intrusions", 
       "Advanced", "[2,7]", "Dr. Lee", "Available"),
      
      (5, "Personal Finance Manager Web App", 
       "Develop a web application for tracking expenses with data visualization", 
       "Beginner", "[3,5]", "Dr. Chen", "Available"),
      
      (6, "Real-Time Multiplayer Game", 
       "Build a browser-based multiplayer game with WebSocket communication", 
       "Advanced", "[1,3]", "Dr. Kumar", "Available"),
      
      (7, "Smart Home Automation System", 
       "IoT-based home automation with mobile app control", 
       "Intermediate", "[4,6]", "Dr. Rahman", "Available"),
      
      (8, "Medical Diagnosis Assistant using ML", 
       "AI system to assist in medical diagnosis based on symptoms", 
       "Advanced", "[2,5]", "Dr. Singh", "Available")
    )
    
    projects.foreach { case (id, title, desc, difficulty, comps, supervisor, status) =>
      session.execute(
        s"""
        INSERT INTO capstone_projects (project_id, title, description, difficulty_level, 
                                       required_competencies, supervisor, status)
        VALUES ($id, '$title', '$desc', '$difficulty', '$comps', '$supervisor', '$status')
        """
      )
    }
    println("✓ Seeded 8 capstone projects")
  }
}