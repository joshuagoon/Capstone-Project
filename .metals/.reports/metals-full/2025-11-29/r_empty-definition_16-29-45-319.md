error id: file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/controllers/AIRecommendationController.scala:`<none>`.
file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/controllers/AIRecommendationController.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/springframework/web/bind/annotation/org/example.
	 -org/example.
	 -scala/Predef.org.example.
offset: 212
uri: file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/controllers/AIRecommendationController.scala
text:
```scala
package org.example.controllers

import org.springframework.web.bind.annotation._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.examp@@le.services.{AIRecommendationService, StudentProfile, SubjectGrade, ProjectRecommendation}
import org.example.data.CapstoneProjects
import scala.beans.BeanProperty

// Response class for API
class RecommendationResponse(
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
class AIRecommendationController {

  @Autowired
  private var aiService: AIRecommendationService = _

  @GetMapping(Array("/ai-recommendations/{studentId}"))
  def getAIRecommendations(@PathVariable studentId: Int): java.util.List[RecommendationResponse] = {
    import scala.jdk.CollectionConverters._
    
    println(s"Fetching AI recommendations for student ID: $studentId")
    
    // TODO: In next phase, fetch real student data from Cassandra
    // For now, using mock data based on student ID
    val studentProfile = getMockStudentProfile(studentId)
    
    // Get all available projects
    val availableProjects = CapstoneProjects.getAll
    
    // Get AI recommendations
    val recommendations = aiService.generateRecommendations(studentProfile, availableProjects)
    
    // Convert to response format
    recommendations.map(rec => 
      new RecommendationResponse(rec.projectId, rec.projectTitle, rec.matchScore, rec.reason)
    ).asJava
  }
  
  @GetMapping(Array("/projects"))
  def getAllProjects(): java.util.List[ProjectResponse] = {
    import scala.jdk.CollectionConverters._
    
    CapstoneProjects.getAll.map(p =>
      new ProjectResponse(
        p.id,
        p.title,
        p.description,
        p.difficultyLevel,
        p.requiredSkills.asJava,
        p.supervisor
      )
    ).asJava
  }
  
  @GetMapping(Array("/projects/{id}"))
  def getProjectById(@PathVariable id: Int): ResponseEntity[ProjectResponse] = {
    import scala.jdk.CollectionConverters._
    
    CapstoneProjects.getById(id) match {
      case Some(p) => ResponseEntity.ok(new ProjectResponse(
        p.id, p.title, p.description, p.difficultyLevel, 
        p.requiredSkills.asJava, p.supervisor
      ))
      case None => ResponseEntity.notFound().build()
    }
  }
  
  // Mock student profiles - replace with Cassandra queries later
  private def getMockStudentProfile(studentId: Int): StudentProfile = {
    studentId match {
      case 1 => StudentProfile(
        id = 1,
        name = "Alice Wong",
        cgpa = 3.82,
        topSubjects = List(
          SubjectGrade("Artificial Intelligence", "A", 88.5),
          SubjectGrade("Machine Learning", "A-", 85.0),
          SubjectGrade("Web Development", "B+", 82.0),
          SubjectGrade("Database Systems", "A", 87.5)
        ),
        interests = "AI, Machine Learning, Data Science"
      )
      
      case 2 => StudentProfile(
        id = 2,
        name = "Bob Tan",
        cgpa = 3.45,
        topSubjects = List(
          SubjectGrade("Mobile Development", "A-", 85.0),
          SubjectGrade("User Interface Design", "A", 88.0),
          SubjectGrade("Cloud Computing", "B+", 83.5),
          SubjectGrade("Software Engineering", "B+", 82.0)
        ),
        interests = "Mobile Apps, Cloud Computing, UI/UX"
      )
      
      case 3 => StudentProfile(
        id = 3,
        name = "Charlie Lee",
        cgpa = 3.92,
        topSubjects = List(
          SubjectGrade("Blockchain Technology", "A", 92.0),
          SubjectGrade("Cryptography", "A", 90.5),
          SubjectGrade("Artificial Intelligence", "A-", 87.0),
          SubjectGrade("Advanced Algorithms", "A", 89.0)
        ),
        interests = "Blockchain, Security, Cryptography"
      )
      
      case _ => StudentProfile(
        id = studentId,
        name = "Student " + studentId,
        cgpa = 3.50,
        topSubjects = List(
          SubjectGrade("Programming Fundamentals", "B+", 82.0),
          SubjectGrade("Data Structures", "B", 78.5),
          SubjectGrade("Web Development", "A-", 85.0)
        ),
        interests = "Web Development, Software Engineering"
      )
    }
  }
}

// Response class for projects
class ProjectResponse(
  @BeanProperty var projectId: Int,
  @BeanProperty var title: String,
  @BeanProperty var description: String,
  @BeanProperty var difficultyLevel: String,
  @BeanProperty var requiredSkills: java.util.List[String],
  @BeanProperty var supervisor: String
) {
  def this() = this(0, "", "", "", new java.util.ArrayList[String](), "")
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.