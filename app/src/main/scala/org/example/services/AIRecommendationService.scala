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

  @Value("${anthropic.api.key:none}")
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
      val response = callClaudeAPI(prompt)
      parseRecommendations(response, availableProjects, studentProfile)
    } catch {
      case e: Exception =>
        println(s"Claude API failed, falling back to rule-based: ${e.getMessage}")
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
    
    // Build CGPA section conditionally
    val cgpaSection = if (student.cgpa > 0.0) {
      s"- Overall CGPA: ${student.cgpa}\n"
    } else {
      "" // Don't mention CGPA if it's 0.0
    }
    
    // Difficulty guidance based on CGPA (only if CGPA exists)
    val difficultyGuidance = if (student.cgpa > 0.0) {
      s"2. Appropriate difficulty level based on CGPA (3.5+ = Advanced, 3.0-3.5 = Intermediate, <3.0 = Beginner)\n"
    } else {
      "2. Appropriate difficulty level based on subject performance\n"
    }
    
    s"""You are an academic advisor helping a computer science student choose the most suitable capstone project.

Student Profile:
- Name: ${student.name}
$cgpaSection- Top Subject Performances:
$subjectsList
${if (student.interests.nonEmpty) s"- Stated Interests: ${student.interests}" else ""}

Available Capstone Projects:
$projectsList

Task: Recommend the TOP 3 most suitable capstone projects for this student.

For each recommendation, consider:
1. Match between student's strong subjects and project's required skills
$difficultyGuidance3. Student's interests alignment
4. Specific grades in relevant subjects

Provide your response as a JSON array with exactly 3 recommendations, ordered by match score (highest first):

[
  {
    "projectId": 1,
    "projectTitle": "exact project title from list above",
    "matchScore": 0.95,
    "reason": "Specific reason mentioning student's grades and strengths. For example: 'Your excellent grade in Artificial Intelligence (A, 88%) demonstrates strong expertise needed for this project.'"
  }
]

IMPORTANT: 
- Use ONLY project IDs and titles from the list above
- Match scores should be between 0.60 and 0.98 (be realistic)
- Reasons must be specific and reference actual student grades/subjects
${if (student.cgpa <= 0.0) "- Do NOT mention CGPA in your reasons since it's not available\n" else ""}- Return ONLY the JSON array, no other text"""
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
      "max_tokens": 2000,
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
    
    println("Calling Claude API...")
    val response = client.send(request, HttpResponse.BodyHandlers.ofString())
    
    println(s"Claude API Status: ${response.statusCode()}")
    
    if (response.statusCode() == 200) {
      val jsonResponse = mapper.readTree(response.body())
      val content = jsonResponse.get("content").get(0).get("text").asText()
      
      println(s"Claude Response: ${content.take(200)}...")
      
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
      
      println(s"Successfully parsed ${recommendations.size} recommendations")
      recommendations.toList.take(3)
      
    } catch {
      case e: Exception =>
        println(s"Error parsing recommendations: ${e.getMessage}")
        e.printStackTrace()
        getRuleBasedRecommendations(student, projects)
    }
  }
  
  /**
   * Fallback: Rule-based recommendation algorithm (no API needed)
   */
  private def getRuleBasedRecommendations(
    student: StudentProfile,
    projects: List[Project]
  ): List[ProjectRecommendation] = {
    
    println("Using rule-based recommendation algorithm")
    
    val scoredProjects = projects.map { project =>
      var score = 0.0
      val reasons = scala.collection.mutable.ListBuffer[String]()
      
      // 1. Difficulty matching (30% weight) - adjusted for missing CGPA
      val difficultyMatch = if (student.cgpa > 0.0) {
        // Use CGPA if available
        project.difficultyLevel.toLowerCase match {
          case "beginner" if student.cgpa < 3.0 => 0.3
          case "intermediate" if student.cgpa >= 3.0 && student.cgpa < 3.6 => 0.3
          case "advanced" if student.cgpa >= 3.6 => 0.3
          case "intermediate" if student.cgpa >= 2.5 => 0.2
          case _ => 0.1
        }
      } else {
        // Use average subject performance if CGPA not available
        val avgPercentage = if (student.topSubjects.nonEmpty) {
          student.topSubjects.map(_.percentage).sum / student.topSubjects.size
        } else {
          70.0 // Default middle value
        }
        
        project.difficultyLevel.toLowerCase match {
          case "beginner" if avgPercentage < 70.0 => 0.3
          case "intermediate" if avgPercentage >= 70.0 && avgPercentage < 85.0 => 0.3
          case "advanced" if avgPercentage >= 85.0 => 0.3
          case "intermediate" if avgPercentage >= 60.0 => 0.2
          case _ => 0.1
        }
      }
      score += difficultyMatch
      
      // Only mention CGPA if it exists
      if (difficultyMatch >= 0.25) {
        if (student.cgpa > 0.0) {
          reasons += s"Appropriate ${project.difficultyLevel.toLowerCase} difficulty for your CGPA of ${student.cgpa}"
        } else {
          reasons += s"Appropriate ${project.difficultyLevel.toLowerCase} difficulty based on your subject performance"
        }
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