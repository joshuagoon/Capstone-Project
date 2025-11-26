package org.example.services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.example.data.Project

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

@Service
class AIRecommendationService {

  @Value("${openai.api.key:none}")
  private var apiKey: String = _
  
  private val client = HttpClient.newHttpClient()
  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  def generateRecommendations(
    studentProfile: StudentProfile,
    availableProjects: List[Project]
  ): List[ProjectRecommendation] = {
    
    println(s"Generating recommendations for student: ${studentProfile.name}")
    
    if (apiKey == null || apiKey.isEmpty || apiKey == "none") {
      println("No API key configured, using rule-based recommendations")
      return getRuleBasedRecommendations(studentProfile, availableProjects)
    }
    
    val prompt = buildPrompt(studentProfile, availableProjects)
    
    try {
      val response = callOpenAIAPI(prompt)
      parseRecommendations(response, availableProjects, studentProfile)
    } catch {
      case e: Exception =>
        println(s"AI API failed, falling back to rule-based: ${e.getMessage}")
        getRuleBasedRecommendations(studentProfile, availableProjects)
    }
  }
  
  private def buildPrompt(student: StudentProfile, projects: List[Project]): String = {
    val projectsList = projects.map(p => 
      s"${p.id}. ${p.title} (${p.difficultyLevel})\n   Description: ${p.description}\n   Required Skills: ${p.requiredSkills.mkString(", ")}"
    ).mkString("\n\n")
    
    val subjectsList = student.topSubjects.map(s =>
      s"  • ${s.subjectName}: ${s.grade} (${s.percentage}%)"
    ).mkString("\n")
    
    s"""You are an academic advisor. Recommend the TOP 3 capstone projects for this student.

Student: ${student.name}
CGPA: ${student.cgpa}
Top Subjects:
$subjectsList
Interests: ${student.interests}

Available Projects:
$projectsList

Return ONLY a JSON array (no markdown, no extra text):
[
  {
    "projectId": 1,
    "projectTitle": "exact title",
    "matchScore": 0.95,
    "reason": "specific reason mentioning grades"
  }
]"""
  }
  
  private def callOpenAIAPI(prompt: String): String = {
    val escapedPrompt = prompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "")
    
    val requestBody = s"""{
      "model": "gpt-3.5-turbo",
      "messages": [
        {
          "role": "system",
          "content": "You are an academic advisor. Always respond with valid JSON only."
        },
        {
          "role": "user",
          "content": "$escapedPrompt"
        }
      ],
      "temperature": 0.7,
      "max_tokens": 1500
    }"""
    
    val request = HttpRequest.newBuilder()
      .uri(URI.create("https://api.openai.com/v1/chat/completions"))
      .header("Content-Type", "application/json")
      .header("Authorization", s"Bearer $apiKey")
      .POST(HttpRequest.BodyPublishers.ofString(requestBody))
      .build()
    
    println("Calling OpenAI API...")
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    
    println(s"OpenAI Status: ${response.statusCode()}")
    
    if (response.statusCode() == 200) {
      val jsonResponse = mapper.readTree(response.body())
      val content = jsonResponse.get("choices").get(0).get("message").get("content").asText()
      
      println(s"OpenAI Response: ${content.take(200)}...")
      
      val jsonStart = content.indexOf("[")
      val jsonEnd = content.lastIndexOf("]") + 1
      
      if (jsonStart >= 0 && jsonEnd > jsonStart) {
        content.substring(jsonStart, jsonEnd)
      } else {
        content
      }
    } else {
      println(s"OpenAI error: ${response.body()}")
      throw new Exception(s"API returned ${response.statusCode()}")
    }
  }
  
  private def parseRecommendations(
    jsonResponse: String, 
    projects: List[Project],
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
      
      println(s"Parsed ${recommendations.size} recommendations")
      recommendations.toList.take(3)
      
    } catch {
      case e: Exception =>
        println(s"Parse error: ${e.getMessage}")
        e.printStackTrace()
        getRuleBasedRecommendations(student, projects)
    }
  }
  
  /**
   * Rule-based recommendation algorithm (no API needed)
   */
  private def getRuleBasedRecommendations(
    student: StudentProfile,
    projects: List[Project]
  ): List[ProjectRecommendation] = {
    
    println("Using rule-based recommendation algorithm")
    
    val scoredProjects = projects.map { project =>
      var score = 0.0
      val reasons = scala.collection.mutable.ListBuffer[String]()
      
      // 1. Difficulty matching (30% weight)
      val difficultyMatch = project.difficultyLevel.toLowerCase match {
        case "beginner" if student.cgpa < 3.0 => 0.3
        case "intermediate" if student.cgpa >= 3.0 && student.cgpa < 3.6 => 0.3
        case "advanced" if student.cgpa >= 3.6 => 0.3
        case "intermediate" if student.cgpa >= 2.5 => 0.2
        case _ => 0.1
      }
      score += difficultyMatch
      
      if (difficultyMatch >= 0.25) {
        reasons += s"Appropriate ${project.difficultyLevel.toLowerCase} difficulty for your CGPA of ${student.cgpa}"
      }
      
      // 2. Subject/skill matching (50% weight)
      val subjectNames = student.topSubjects.map(_.subjectName.toLowerCase).toSet
      val matchingSkills = project.requiredSkills.filter(skill =>
        subjectNames.exists(subj => 
          subj.contains(skill.toLowerCase) || skill.toLowerCase.contains(subj)
        )
      )
      
      if (matchingSkills.nonEmpty) {
        val skillScore = (matchingSkills.size.toDouble / project.requiredSkills.size) * 0.5
        score += skillScore
        
        val topMatch = student.topSubjects.find(s => 
          matchingSkills.exists(skill => 
            s.subjectName.toLowerCase.contains(skill.toLowerCase)
          )
        )
        
        topMatch.foreach(s => 
          reasons += s"Your strong performance in ${s.subjectName} (${s.grade}, ${s.percentage}%) matches project requirements"
        )
      }
      
      // 3. Interest matching (20% weight)
      if (student.interests != null && student.interests.nonEmpty) {
        val interests = student.interests.toLowerCase.split(",").map(_.trim)
        val interestMatches = interests.count(interest =>
          project.title.toLowerCase.contains(interest) ||
          project.description.toLowerCase.contains(interest) ||
          project.requiredSkills.exists(_.toLowerCase.contains(interest))
        )
        
        if (interestMatches > 0) {
          score += 0.2
          reasons += s"Aligns with your interests in ${student.interests}"
        }
      }
      
      val finalReason = if (reasons.isEmpty) {
        "Good match for your academic profile"
      } else {
        reasons.mkString(". ")
      }
      
      ProjectRecommendation(project.id, project.title, score, finalReason)
    }
    
    scoredProjects.sortBy(rec => -rec.matchScore).take(3)
  }
}