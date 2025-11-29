file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/controllers/AIRecommendationController.scala
### java.lang.AssertionError: NoDenotation.owner

occurred in the presentation compiler.

presentation compiler configuration:


action parameters:
uri: file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/controllers/AIRecommendationController.scala
text:
```scala
package org.example.controllers

import org.springframework.web.bind.annotation._
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.{ResponseEntity, HttpStatus}
import org.example.services.{AIRecommendationService, StudentDataService, StudentProfile}
import org.example.data.CapstoneProjects
import org.example.repositories.RealStudentRepository
import scala.beans.BeanProperty
import scala.jdk.CollectionConverters._

// Student response for API
class StudentResponse(
  @BeanProperty var studentId: Int,
  @BeanProperty var name: String,
  @BeanProperty var email: String,
  @BeanProperty var program: String,
  @BeanProperty var cohort: Int,
  @BeanProperty var cgpa: Double
) {
  def this() = this(0, "", "", "", 0, 0.0)
}

// Login request
class LoginRequest(
  @BeanProperty var studentId: Int
) {
  def this() = this(0)
}

// Login response
class LoginResponse(
  @BeanProperty var success: Boolean,
  @BeanProperty var message: String,
  @BeanProperty var student: StudentResponse
) {
  def this() = this(false, "", null)
}

// Recommendation response
class RecommendationResponse(
  @BeanProperty var projectId: Int,
  @BeanProperty var projectTitle: String,
  @BeanProperty var score: Double,
  @BeanProperty var reason: String
) {
  def this() = this(0, "", 0.0, "")
}

// Project response
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

@RestController
@RequestMapping(Array("/api"))
@CrossOrigin(origins = Array("http://localhost:3000"))
class AIRecommendationController {

  @Autowired
  private var aiService: AIRecommendationService = _
  
  @Autowired
  private var studentDataService: StudentDataService = _
  
  @Autowired
  private var studentRepository: RealStudentRepository = _

  /**
   * Login endpoint - verify student ID exists
   */
  @PostMapping(Array("/login"))
  def login(@RequestBody loginRequest: LoginRequest): ResponseEntity[LoginResponse] = {
    val studentId = loginRequest.studentId
    
    println(s"Login attempt for student ID: $studentId")
    
    studentDataService.getStudentById(studentId) match {
      case Some(student) =>
        val response = new LoginResponse(
          success = true,
          message = "Login successful",
          student = new StudentResponse(
            studentId = student.id,
            name = if (student.name != null) student.name else s"Student ${student.id}",
            email = s"student${student.id}@sunway.edu.my",
            program = if (student.programme != null) student.programme else "Computer Science",
            cohort = student.cohort,
            cgpa = if (student.overallCgpa != null) student.overallCgpa.doubleValue() else 0.0
          )
        )
        ResponseEntity.ok(response)
        
      case None =>
        val response = new LoginResponse(
          success = false,
          message = s"Student ID ${studentId} not found",
          student = null
        )
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }
  }

  /**
   * Get student by ID
   */
  @GetMapping(Array("/students/{id}"))
  def getStudentById(@PathVariable id: Int): ResponseEntity[StudentResponse] = {
    studentDataService.getStudentById(id) match {
      case Some(student) =>
        val response = new StudentResponse(
          studentId = student.id,
          name = if (student.name != null) student.name else s"Student ${student.id}",
          email = s"student${student.id}@sunway.edu.my",
          program = if (student.programme != null) student.programme else "Computer Science",
          cohort = student.cohort,
          cgpa = if (student.overallCgpa != null) student.overallCgpa.doubleValue() else 0.0
        )
        ResponseEntity.ok(response)
        
      case None =>
        ResponseEntity.notFound().build()
    }
  }

  /**
   * Get student performance data from real Cassandra database
   */
  @GetMapping(Array("/performance/{studentId}"))
  def getStudentPerformance(@PathVariable studentId: Int): ResponseEntity[java.util.Map[String, Any]] = {
    println(s"Fetching performance for student ID: $studentId")
    
    studentDataService.getStudentPerformance(studentId) match {
      case Some(perfMap) =>
        val javaMap = new java.util.HashMap[String, Any]()
        perfMap.foreach { case (k, v) =>
          v match {
            case list: List[_] => 
              val javaList = new java.util.ArrayList[Any]()
              list.foreach {
                case map: Map[_, _] =>
                  val innerMap = new java.util.HashMap[String, Any]()
                  map.asInstanceOf[Map[String, Any]].foreach { case (ik, iv) =>
                    innerMap.put(ik, iv)
                  }
                  javaList.add(innerMap)
                case other => javaList.add(other)
              }
              javaMap.put(k, javaList)
            case other => javaMap.put(k, other)
          }
        }
        ResponseEntity.ok(javaMap)
        
      case None =>
        val errorMap = new java.util.HashMap[String, Any]()
        errorMap.put("error", s"Student $studentId not found")
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMap)
    }
  }

  /**
   * Get AI-powered recommendations using REAL student data from Cassandra
   */
  @GetMapping(Array("/ai-recommendations/{studentId}"))
  def getAIRecommendations(@PathVariable studentId: Int): ResponseEntity[java.util.List[RecommendationResponse]] = {
    println(s"Fetching AI recommendations for student ID: $studentId")
    
    studentDataService.buildStudentProfile(studentId) match {
      case Some(studentProfile) =>
        println(s"Built profile for ${studentProfile.name} with CGPA ${studentProfile.cgpa}")
        println(s"Top subjects: ${studentProfile.topSubjects.map(_.subjectName).mkString(", ")}")
        
        val availableProjects = CapstoneProjects.getAll
        val recommendations = aiService.generateRecommendations(studentProfile, availableProjects)
        
        val response = recommendations.map(rec => 
          new RecommendationResponse(rec.projectId, rec.projectTitle, rec.matchScore, rec.reason)
        ).asJava
        
        ResponseEntity.ok(response)
        
      case None =>
        println(s"Student $studentId not found in database")
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Collections.emptyList[RecommendationResponse]())
    }
  }
  
  /**
   * Get all available capstone projects
   */
  @GetMapping(Array("/projects"))
  def getAllProjects(): java.util.List[ProjectResponse] = {
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
  
  /**
   * Get project by ID
   */
  @GetMapping(Array("/projects/{id}"))
  def getProjectById(@PathVariable id: Int): ResponseEntity[ProjectResponse] = {
    CapstoneProjects.getById(id) match {
      case Some(p) => ResponseEntity.ok(new ProjectResponse(
        p.id, p.title, p.description, p.difficultyLevel, 
        p.requiredSkills.asJava, p.supervisor
      ))
      case None => ResponseEntity.notFound().build()
    }
  }
  
  /**
   * Health check endpoint
   */
  @GetMapping(Array("/health"))
  def health(): java.util.Map[String, String] = {
    val healthMap = new java.util.HashMap[String, String]()
    healthMap.put("status", "UP")
    healthMap.put("database", "connected")
    
    try {
      val count = studentRepository.count()
      healthMap.put("studentCount", count.toString)
    } catch {
      case e: Exception =>
        healthMap.put("database", "error")
        healthMap.put("error", e.getMessage)
    }
    
    healthMap
  }
}
```



#### Error stacktrace:

```
dotty.tools.dotc.core.SymDenotations$NoDenotation$.owner(SymDenotations.scala:2609)
	dotty.tools.dotc.core.SymDenotations$SymDenotation.isSelfSym(SymDenotations.scala:715)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:330)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1669)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1677)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1675)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:454)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1677)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1675)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$13(ExtractSemanticDB.scala:391)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:386)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1669)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1677)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.fold$1(Trees.scala:1636)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.apply(Trees.scala:1638)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1675)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1669)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:457)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1724)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:354)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$11(ExtractSemanticDB.scala:377)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:377)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.apply(Trees.scala:1770)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1728)
	dotty.tools.dotc.ast.Trees$Instance$TreeAccumulator.foldOver(Trees.scala:1642)
	dotty.tools.dotc.ast.Trees$Instance$TreeTraverser.traverseChildren(Trees.scala:1771)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:351)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse$$anonfun$1(ExtractSemanticDB.scala:315)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:15)
	scala.runtime.function.JProcedure1.apply(JProcedure1.java:10)
	scala.collection.immutable.List.foreach(List.scala:334)
	dotty.tools.dotc.semanticdb.ExtractSemanticDB$Extractor.traverse(ExtractSemanticDB.scala:315)
	dotty.tools.pc.SemanticdbTextDocumentProvider.textDocument(SemanticdbTextDocumentProvider.scala:36)
	dotty.tools.pc.ScalaPresentationCompiler.semanticdbTextDocument$$anonfun$1(ScalaPresentationCompiler.scala:242)
```
#### Short summary: 

java.lang.AssertionError: NoDenotation.owner