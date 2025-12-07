package org.example.services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import scala.util.Random

case class StudentProfile(
  id: Int,
  name: String,
  cgpa: Double,
  topSubjects: List[SubjectGrade],
  interests: String
)

case class SubjectGrade(
  subjectName: String,
  grade: String,
  percentage: Double
)

case class ProjectRecommendation(
  projectId: Int,
  projectTitle: String,
  matchScore: Double,
  reason: String
)

case class UserPreferences(
  interests: Option[String],
  preferredDifficulty: Option[String],
  avoidTopics: Option[String],
  additionalNotes: Option[String]
)

@Service
class AIRecommendationService {

  @Value("${anthropic.api.key:none}")
  private var apiKey: String = _
  
  private val client = HttpClient.newHttpClient()
  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def generateRecommendations(
    studentProfile: StudentProfile,
    excludeProjectIds: List[Int] = List.empty,
    userPreferences: UserPreferences = UserPreferences(None, None, None, None)
  ): List[ProjectRecommendation] = {
    
    println(s"Generating AI-powered recommendations for student: ${studentProfile.name}")
    println(s"User preferences: $userPreferences")
    
    if (apiKey == null || apiKey.isEmpty || apiKey == "none") {
      println("ERROR: No API key configured! AI generation requires Claude API.")
      return getFallbackRecommendations(studentProfile, userPreferences)
    }
    
    val prompt = buildCreativePrompt(studentProfile, userPreferences)
    
    try {
      val response = callClaudeAPI(prompt)
      parseRecommendations(response, studentProfile)
    } catch {
      case e: Exception =>
        println(s"Claude API failed: ${e.getMessage}")
        e.printStackTrace()
        getFallbackRecommendations(studentProfile, userPreferences)
    }
  }
  
  private def buildCreativePrompt(
    student: StudentProfile,
    userPrefs: UserPreferences
  ): String = {
    
    val studentContext = buildStudentContext(student, userPrefs)
    
    val difficultyGuidance = userPrefs.preferredDifficulty match {
      case Some(diff) => s"They specifically want a ${diff} difficulty project."
      case None if student.cgpa > 0.0 =>
        if (student.cgpa >= 3.5) "They can handle advanced, challenging projects."
        else if (student.cgpa >= 3.0) "They're ready for intermediate to advanced projects."
        else "They should start with beginner to intermediate projects."
      case None =>
        val avgPercentage = if (student.topSubjects.nonEmpty) {
          student.topSubjects.map(_.percentage).sum / student.topSubjects.size
        } else {
          70.0
        }
        if (avgPercentage >= 85) "They can handle advanced, challenging projects."
        else if (avgPercentage >= 70) "They're ready for intermediate to advanced projects."
        else "They should start with beginner to intermediate projects."
    }
    
    val avoidanceGuidance = userPrefs.avoidTopics match {
      case Some(avoid) => s"\n⚠️ IMPORTANT: Do NOT suggest projects related to: ${avoid}"
      case None => ""
    }
    
    s"""You are a creative academic advisor who designs personalized capstone projects for university students.

STUDENT PROFILE:
${studentContext}

${difficultyGuidance}
${avoidanceGuidance}

YOUR TASK:
Design 3 COMPLETELY ORIGINAL capstone project ideas specifically for ${student.name}. These should be:
- Tailored to their unique strengths and interests
- Realistic and achievable within one semester
- Technically challenging but not overwhelming
- Industry-relevant and valuable for their portfolio
- Different from each other (don't suggest similar projects)

For each project:
1. Create an innovative, specific project title
2. Explain why THIS student would excel at THIS project
3. Connect it to their coursework and grades
4. Show how it builds on their strengths
5. Make them excited about working on it

THINK CREATIVELY:
- What kind of projects would showcase THEIR specific skills?
- What problems could THEY solve given their background?
- What would make THEM stand out to employers?
- How can you combine their different strengths in interesting ways?

GUIDELINES:
✓ Be specific - not generic projects anyone could do
✓ Reference actual courses and grades from their profile
✓ Make each project unique and distinct
✓ Be encouraging and show genuine enthusiasm
✓ Consider current technology trends and industry needs
${userPrefs.interests.map(i => s"✓ MUST incorporate their stated interest: $i").getOrElse("")}
${userPrefs.additionalNotes.map(n => s"✓ Consider: $n").getOrElse("")}
${if (student.cgpa <= 0.0) "✓ Don't mention CGPA (focus on course performance)" else ""}

Return your response as JSON:
[
  {
    "projectId": [generate a random number between 1000-9999],
    "projectTitle": "[Your creative, specific project title]",
    "matchScore": [realistic confidence 0.70-0.95],
    "reason": "[Your enthusiastic, personalized explanation - make it conversational and specific to THIS student]"
  }
]

IMPORTANT:
- Each project should be COMPLETELY DIFFERENT
- Don't suggest variations of the same idea
- Be creative and think outside the box
- Make ${student.name} feel like these were designed just for them

Return ONLY the JSON array, nothing else."""
  }
  
  private def buildStudentContext(student: StudentProfile, prefs: UserPreferences): String = {
    val sections = scala.collection.mutable.ListBuffer[String]()
    
    sections += s"Name: ${student.name}"
    
    // Academic performance with specific grades
    if (student.topSubjects.nonEmpty) {
      val courseDetails = student.topSubjects.map(s => 
        s"  - ${s.subjectName}: ${s.grade} grade (${s.percentage}%)"
      ).mkString("\n")
      sections += s"Top Courses:\n$courseDetails"
    }
    
    if (student.cgpa > 0.0) {
      sections += s"Overall CGPA: ${student.cgpa}"
    }
    
    // Academic interests
    if (student.interests.nonEmpty) {
      sections += s"Academic Areas: ${student.interests}"
    }
    
    // User's specific interests and goals
    prefs.interests.foreach(i => 
      sections += s"Specific Interests: $i"
    )
    
    prefs.additionalNotes.foreach(n => 
      sections += s"Additional Context: $n"
    )
    
    sections.mkString("\n")
  }
  
  private def callClaudeAPI(prompt: String): String = {
    val escapedPrompt = prompt
      .replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\n", "\\n")
      .replace("\r", "")
      .replace("\t", "\\t")
    
    val requestBody = s"""{
      "model": "claude-sonnet-4-20250514",
      "max_tokens": 3000,
      "temperature": 0.9,
      "messages": [
        {
          "role": "user",
          "content": "$escapedPrompt"
        }
      ]
    }"""
    
    val request = HttpRequest.newBuilder()
      .uri(URI.create("https://api.anthropic.com/v1/messages"))
      .header("Content-Type", "application/json")
      .header("x-api-key", apiKey)
      .header("anthropic-version", "2023-06-01")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()
    
    println("Calling Claude API for creative project generation...")
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    
    println(s"Claude API Status: ${response.statusCode()}")
    
    if (response.statusCode() == 200) {
      val jsonResponse = mapper.readTree(response.body())
      val content = jsonResponse.get("content").get(0).get("text").asText()
      
      println(s"Claude Response: ${content.take(300)}...")
      
      val jsonStart = content.indexOf("[")
      val jsonEnd = content.lastIndexOf("]") + 1
      
      if (jsonStart >= 0 && jsonEnd > jsonStart) {
        content.substring(jsonStart, jsonEnd)
      } else {
        println("Warning: Could not find JSON array in response")
        content
      }
    } else {
      println(s"Claude API error: ${response.statusCode()}")
      println(s"Response: ${response.body()}")
      throw new Exception(s"API returned ${response.statusCode()}")
    }
  }
  
  private def parseRecommendations(
    jsonResponse: String,
    student: StudentProfile
  ): List[ProjectRecommendation] = {
    try {
      import scala.jdk.CollectionConverters._
      
      val jsonNode = mapper.readTree(jsonResponse)
      val recommendations = scala.collection.mutable.ListBuffer[ProjectRecommendation]()
      
      jsonNode.elements().asScala.foreach { node =>
        val projectId = node.get("projectId").asInt()
        val title = node.get("projectTitle").asText()
        val score = node.get("matchScore").asDouble()
        val reason = node.get("reason").asText()
        
        recommendations += ProjectRecommendation(projectId, title, score, reason)
      }
      
      println(s"Successfully generated ${recommendations.size} unique project recommendations")
      
      recommendations.toList.take(3)
      
    } catch {
      case e: Exception =>
        println(s"Error parsing AI recommendations: ${e.getMessage}")
        e.printStackTrace()
        getFallbackRecommendations(student, UserPreferences(None, None, None, None))
    }
  }
  
  /**
   * Fallback: Generate generic recommendations when API is unavailable
   */
  private def getFallbackRecommendations(
    student: StudentProfile,
    userPrefs: UserPreferences
  ): List[ProjectRecommendation] = {
    
    println("Using fallback generic recommendations")
    
    val random = new Random()
    
    // Generate generic projects based on student's top subjects
    val topSubject = if (student.topSubjects.nonEmpty) {
      student.topSubjects.head.subjectName
    } else {
      "Software Development"
    }
    
    val fallbackProjects = List(
      ProjectRecommendation(
        projectId = random.nextInt(9000) + 1000,
        projectTitle = s"AI-Powered Application Using $topSubject Concepts",
        matchScore = 0.75,
        reason = s"This project would leverage your strong performance in $topSubject to build a practical application that demonstrates your skills."
      ),
      ProjectRecommendation(
        projectId = random.nextInt(9000) + 1000,
        projectTitle = "Full-Stack Web Development Project",
        matchScore = 0.72,
        reason = "A comprehensive web application that showcases your programming abilities and understanding of modern software development practices."
      ),
      ProjectRecommendation(
        projectId = random.nextInt(9000) + 1000,
        projectTitle = "Data Analysis and Visualization System",
        matchScore = 0.70,
        reason = "Build a system that analyzes real-world data and presents insights through interactive visualizations, applying your analytical skills."
      )
    )
    
    fallbackProjects
  }
}