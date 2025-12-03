package org.example.services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Autowired
import org.example.repositories.{RealStudentRepository, RealSubjectRepository}
import org.example.models.{RealStudent, RealSubject}
import scala.jdk.CollectionConverters._

@Service
class StudentDataService {

  @Autowired
  private var studentRepository: RealStudentRepository = _
  
  @Autowired
  private var subjectRepository: RealSubjectRepository = _

  /**
   * Get student by ID from Cassandra
   */
  def getStudentById(studentId: Int): Option[RealStudent] = {
    val result = studentRepository.findById(studentId)
    if (result.isPresent) Some(result.get()) else None
  }

  /**
   * Build StudentProfile for AI recommendations from real Cassandra data
   */
  def buildStudentProfile(studentId: Int): Option[StudentProfile] = {
    getStudentById(studentId).map { student =>
      
      // Get all subjects from database
      val studentSubjects = subjectRepository
        .findByStudentId(studentId)
        .asScala
        .toList
        .filter(s => s.grade != null && s.overallPercentage != null)
        .sortBy(-_.overallPercentage.doubleValue())
        .take(10) // Top 10 subjects
      
      // Convert to SubjectGrade format for AI
      val topSubjects = studentSubjects.map(s =>
        SubjectGrade(
          subjectName = s.subjectName,
          grade = s.grade,
          percentage = if (s.overallPercentage != null) s.overallPercentage.doubleValue() else 0.0
        )
      ).take(5) // Top 5 for AI recommendation
      
      // Extract interests from subject names (basic heuristic)
      val interests = extractInterests(studentSubjects)
      
      StudentProfile(
        id = student.id,
        name = if (student.name != null) student.name else s"Student ${student.id}",
        cgpa = if (student.overallCgpa != null) student.overallCgpa.doubleValue() else 0.0,
        topSubjects = topSubjects,
        interests = interests
      )
    }
  }
  
  /**
   * Extract likely interests from subject performance
   */
  private def extractInterests(subjects: List[RealSubject]): String = {
    val keywords = Map(
      "Artificial Intelligence" -> List("artificial intelligence", "machine learning", "computational intelligence", "computer vision", "neural"),
      "Web Development" -> List("web", "html", "javascript", "internet", "frontend", "backend"),
      "Mobile Development" -> List("mobile", "android", "ios", "app development", "application development"),
      "Data Science & Analytics" -> List("data science", "analytics", "statistics", "data mining", "business intelligence", "big data"),
      "Cybersecurity" -> List("security", "cryptography", "cyber", "hacking", "forensic", "ethical hacking"),
      "Database Systems" -> List("database", "sql", "data management", "information systems"),
      "Software Engineering" -> List("software engineering", "software architecture", "design pattern", "testing", "capstone"),
      "Networks" -> List("network", "distributed systems", "communication systems"),
      "Cloud Computing" -> List("cloud", "aws", "azure"),
      "UI/UX Design" -> List("ui/ux", "user interface", "design thinking", "multimedia", "human computer interaction")
    )
    
    val subjectNames = subjects.map(_.subjectName.toLowerCase)
    
    val matchedInterests = keywords.filter { case (interest, keywordList) =>
      keywordList.exists(keyword => subjectNames.exists(_.contains(keyword)))
    }.keys.toList
    
    if (matchedInterests.nonEmpty) {
      matchedInterests.take(3).mkString(", ")
    } else {
      "Software Engineering, Programming"
    }
  }
  
  /**
   * Get performance data for frontend dashboard
   */
  def getStudentPerformance(studentId: Int): Option[Map[String, Any]] = {
    getStudentById(studentId).map { student =>
      
      // Get student's subjects
      val studentSubjects = subjectRepository
        .findByStudentId(studentId)
        .asScala
        .toList
        .filter(s => s.grade != null)
      
      // Calculate competencies from subjects
      val competencies = calculateCompetencies(studentSubjects)
      
      // Get recent grades
      val recentGrades = studentSubjects
        .filter(s => s.examYear != 0 && s.examMonth != null)
        .sortBy(s => (
          s.examYear,
          try { s.examMonth.toInt } catch { case _: Exception => 0 }
        ))
        .reverse
        .take(5)
        .map(s => Map(
          "course" -> s"${s.subjectCode} - ${s.subjectName}",
          "grade" -> s.grade,
          "percentage" -> (if (s.overallPercentage != null) s.overallPercentage.doubleValue() else 0.0),
          "semester" -> s"${s.examMonth}/${s.examYear}"
        ))
      
      Map(
        "studentId" -> studentId,
        "gpa" -> (if (student.overallCgpa != null) student.overallCgpa.doubleValue() else 0.0),
        "completedCourses" -> studentSubjects.size,
        "competencies" -> competencies,
        "grades" -> recentGrades
      )
    }
  }
  
  /**
   * Calculate competencies from subjects
   */
  private def calculateCompetencies(subjects: List[RealSubject]): List[Map[String, Any]] = {
    val competencyAreas = Map(
      // Core Programming & Software Development
      "Programming & Software Development" -> List(
        "programming", "code", "software", "object-oriented", "functional", 
        "concurrent", "embedded", "c#", "problem solving"
      ),
      
      // Artificial Intelligence & Machine Learning
      "Artificial Intelligence" -> List(
        "artificial intelligence", "machine learning", "computational intelligence",
        "computer vision", "neural"
      ),
      
      // Data Science & Analytics
      "Data Science & Analytics" -> List(
        "data mining", "knowledge discovery", "business intelligence", 
        "analytics", "statistics", "big data", "visual analytics"
      ),
      
      // Web & Mobile Development
      "Web & Mobile Development" -> List(
        "web", "mobile", "html", "javascript", "internet", "commerce",
        "application development"
      ),
      
      // Database & Information Systems
      "Database Management" -> List(
        "database", "sql", "data management", "information systems",
        "customer relationship", "crm"
      ),
      
      // Networks & Security
      "Networks & Cybersecurity" -> List(
        "network", "security", "cyber", "hacking", "forensic", "distributed",
        "cryptography", "ethical hacking"
      ),
      
      // Software Engineering
      "Software Engineering" -> List(
        "software engineering", "software architecture", "design pattern",
        "requirement", "testing", "software process", "capstone"
      ),
      
      // Digital Systems & Hardware  
      "Digital Systems & Electronics" -> List(
        "digital", "electronic", "microprocessor", "embedded system",
        "circuit", "signal processing", "computer organisation"
      ),
      
      // Project Management & Business
      "Project Management" -> List(
        "project management", "entrepreneurship", "management"
      ),
      
      // Human-Computer Interaction
      "UI/UX Design" -> List(
        "human computer interaction", "ui/ux", "user interface",
        "design thinking", "multimedia"
      )
    )
    
    competencyAreas.map { case (competency, keywords) =>
      val matchingSubjects = subjects.filter(s =>
        keywords.exists(kw => s.subjectName.toLowerCase.contains(kw))
      )
      
      if (matchingSubjects.nonEmpty) {
        val avgScore = matchingSubjects.map(s => 
          if (s.overallPercentage != null) s.overallPercentage.doubleValue() else 0.0
        ).sum / matchingSubjects.size
        
        val level = if (avgScore >= 90) "Expert"
                  else if (avgScore >= 75) "Advanced"
                  else if (avgScore >= 60) "Intermediate"
                  else "Beginner"
        
        Some(Map(
          "name" -> competency,
          "level" -> level,
          "score" -> avgScore.toInt
        ))
      } else {
        None
      }
    }.flatten.toList.sortBy(m => -m("score").asInstanceOf[Int]).take(5)
  }
}