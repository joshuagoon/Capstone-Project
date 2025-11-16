package org.example.controllers

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._
import scala.beans.BeanProperty

// Add @BeanProperty to all fields for proper JSON serialization
class MockStudent(
  @BeanProperty var studentId: Int,
  @BeanProperty var name: String,
  @BeanProperty var email: String,
  @BeanProperty var program: String,
  @BeanProperty var enrolmentYear: Int,
  @BeanProperty var interests: String
) {
  def this() = this(0, "", "", "", 0, "")
}

class MockProject(
  @BeanProperty var projectId: Int,
  @BeanProperty var title: String,
  @BeanProperty var description: String,
  @BeanProperty var difficultyLevel: String,
  @BeanProperty var requiredCompetencies: java.util.List[String],
  @BeanProperty var supervisor: String,
  @BeanProperty var status: String
) {
  def this() = this(0, "", "", "", new java.util.ArrayList[String](), "", "")
}

class MockRecommendation(
  @BeanProperty var projectId: Int,
  @BeanProperty var projectTitle: String,
  @BeanProperty var score: Double,
  @BeanProperty var reason: String
) {
  def this() = this(0, "", 0.0, "")
}

@RestController
@RequestMapping(Array("/api"))
@CrossOrigin(origins = Array("http://localhost:3000"))
class MockDataController {

  @GetMapping(Array("/students"))
  def getAllStudents(): java.util.List[MockStudent] = {
    java.util.Arrays.asList(
      new MockStudent(1, "Alice Wong", "alice.wong@sunway.edu.my", "Computer Science", 2022, "AI, Web Dev"),
      new MockStudent(2, "Bob Tan", "bob.tan@sunway.edu.my", "Computer Science", 2022, "Mobile Dev, Cloud"),
      new MockStudent(3, "Charlie Lee", "charlie.lee@sunway.edu.my", "Computer Science", 2021, "ML, Data Science")
    )
  }

  @GetMapping(Array("/students/{id}"))
  def getStudentById(@PathVariable id: Int): ResponseEntity[MockStudent] = {
    import scala.jdk.CollectionConverters._
    getAllStudents().asScala.find(_.studentId == id) match {
      case Some(student) => ResponseEntity.ok(student)
      case None => ResponseEntity.notFound().build()
    }
  }

  @GetMapping(Array("/projects"))
  def getAllProjects(): java.util.List[MockProject] = {
    java.util.Arrays.asList(
      new MockProject(1, "AI Chatbot", "Build an intelligent chatbot using NLP", "Intermediate", 
                java.util.Arrays.asList("AI", "Programming"), "Dr. Chin", "Available"),
      new MockProject(2, "E-Commerce Platform", "Full-stack e-commerce with recommendations", "Advanced",
                java.util.Arrays.asList("Web Dev", "AI", "Cloud"), "Dr. Wong", "Available"),
      new MockProject(3, "Fitness Tracking App", "Mobile app for health analytics", "Intermediate",
                java.util.Arrays.asList("Mobile Dev", "Data Science"), "Dr. Tan", "Available"),
      new MockProject(4, "Cybersecurity System", "ML-based threat detection", "Advanced",
                java.util.Arrays.asList("AI", "Security"), "Dr. Lee", "Available"),
      new MockProject(5, "Finance Manager", "Personal finance web app", "Beginner",
                java.util.Arrays.asList("Web Dev", "Data Visualization"), "Dr. Chen", "Available")
    )
  }

  @GetMapping(Array("/projects/{id}"))
  def getProjectById(@PathVariable id: Int): ResponseEntity[MockProject] = {
    import scala.jdk.CollectionConverters._
    getAllProjects().asScala.find(_.projectId == id) match {
      case Some(project) => ResponseEntity.ok(project)
      case None => ResponseEntity.notFound().build()
    }
  }

  @GetMapping(Array("/recommendations/{studentId}"))
  def getRecommendations(@PathVariable studentId: Int): java.util.List[MockRecommendation] = {
    java.util.Arrays.asList(
      new MockRecommendation(1, "AI Chatbot", 0.92, "Matches your interest in AI"),
      new MockRecommendation(2, "E-Commerce Platform", 0.85, "Combines AI and Web Development"),
      new MockRecommendation(3, "Fitness Tracking App", 0.78, "Good for data analytics experience")
    )
  }

  @GetMapping(Array("/performance/{studentId}"))
  def getStudentPerformance(@PathVariable studentId: Int): java.util.Map[String, Any] = {
    val performanceMap = new java.util.HashMap[String, Any]()
    performanceMap.put("studentId", studentId.asInstanceOf[Any])
    performanceMap.put("gpa", 3.8.asInstanceOf[Any])
    performanceMap.put("completedCourses", 24.asInstanceOf[Any])
    
    val competencies = java.util.Arrays.asList(
      createMap("name" -> "Artificial Intelligence", "level" -> "Advanced", "score" -> 88),
      createMap("name" -> "Web Development", "level" -> "Intermediate", "score" -> 82),
      createMap("name" -> "Data Science", "level" -> "Intermediate", "score" -> 85)
    )
    performanceMap.put("competencies", competencies)
    
    val grades = java.util.Arrays.asList(
      createMap("course" -> "CS301 - AI", "grade" -> "A", "semester" -> "Fall 2023"),
      createMap("course" -> "CS302 - Web Dev", "grade" -> "B+", "semester" -> "Fall 2023"),
      createMap("course" -> "CS401 - ML", "grade" -> "A-", "semester" -> "Spring 2024")
    )
    performanceMap.put("grades", grades)
    
    performanceMap
  }
  
  private def createMap(entries: (String, Any)*): java.util.Map[String, Any] = {
    val map = new java.util.HashMap[String, Any]()
    entries.foreach { case (k, v) => map.put(k, v.asInstanceOf[Any]) }
    map
  }
}