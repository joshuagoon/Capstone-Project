package org.example.services

import org.springframework.stereotype.Service
import org.springframework.beans.factory.annotation.Value
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.net.URI
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import org.example.data.Project
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
    availableProjects: List[Project],
    excludeProjectIds: List[Int] = List.empty,
    userPreferences: UserPreferences = UserPreferences(None, None, None, None)
  ): List[ProjectRecommendation] = {
    
    println(s"Generating recommendations for student: ${studentProfile.name}")
    println(s"Excluding projects: ${excludeProjectIds.mkString(", ")}")
    println(s"User preferences: $userPreferences")
    
    if (apiKey == null || apiKey.isEmpty || apiKey == "none") {
      println("No API key configured, using rule-based recommendations")
      return getRuleBasedRecommendations(studentProfile, availableProjects, excludeProjectIds, userPreferences)
    }
    
    // Filter out excluded projects
    val filteredProjects = availableProjects.filterNot(p => excludeProjectIds.contains(p.id))
    
    // Further filter by user preferences (avoid topics)
    val finalProjects = userPreferences.avoidTopics match {
      case Some(avoid) =>
        val avoidKeywords = avoid.toLowerCase.split(",").map(_.trim)
        filteredProjects.filterNot(p =>
          avoidKeywords.exists(keyword =>
            p.title.toLowerCase.contains(keyword) ||
            p.description.toLowerCase.contains(keyword) ||
            p.requiredSkills.exists(_.toLowerCase.contains(keyword))
          )
        )
      case None => filteredProjects
    }
    
    if (finalProjects.size < 3) {
      println(s"Warning: Only ${finalProjects.size} projects available after exclusions and filters")
    }
    
    val prompt = buildPrompt(studentProfile, finalProjects, userPreferences)
    
    try {
      val response = callClaudeAPI(prompt)
      parseRecommendations(response, finalProjects, studentProfile)
    } catch {
      case e: Exception =>
        println(s"Claude API failed, falling back to rule-based: ${e.getMessage}")
        getRuleBasedRecommendations(studentProfile, finalProjects, excludeProjectIds, userPreferences)
    }
  }
  
  private def buildPrompt(
    student: StudentProfile, 
    projects: List[Project],
    userPrefs: UserPreferences
  ): String = {
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
      ""
    }
    
    // Build user preferences section
    val preferencesSection = buildPreferencesSection(userPrefs)
    
    // Difficulty guidance based on CGPA or user preference
    val difficultyGuidance = userPrefs.preferredDifficulty match {
      case Some(diff) => s"2. PRIORITIZE ${diff} difficulty level as explicitly requested by the student"
      case None if student.cgpa > 0.0 => 
        "2. Appropriate difficulty level based on CGPA (3.5+ = Advanced, 3.0-3.5 = Intermediate, <3.0 = Beginner)"
      case None => 
        "2. Appropriate difficulty level based on subject performance"
    }
    
    // Add randomness to get different recommendations each time
    val randomSeed = Random.nextInt(1000)
    
    s"""You are an academic advisor helping a university student choose the most suitable capstone project based on their academic background, interests, and personal preferences.

Student Profile:
- Name: ${student.name}
${cgpaSection}- Top Subject Performances:
${subjectsList}
${if (student.interests.nonEmpty) s"- Stated Interests: ${student.interests}" else ""}

${preferencesSection}

Available Capstone Projects:
${projectsList}

Task: Recommend the TOP 3 most suitable capstone projects for this student.

IMPORTANT: Provide DIVERSE recommendations. Each of the 3 projects should be different and appeal to different aspects of the student's profile.

For each recommendation, consider:
1. Match between student's strong subjects and project's required skills
${difficultyGuidance}
3. Student's stated interests and preferences
4. Specific grades in relevant subjects
5. Variety - choose projects from different domains/topics
${userPrefs.interests.map(i => s"6. PRIORITIZE student's specific interests: $i").getOrElse("")}
${userPrefs.additionalNotes.map(n => s"7. Consider student's additional notes: $n").getOrElse("")}

Provide your response as a JSON array with exactly 3 DIFFERENT recommendations, ordered by match score (highest first):

[
  {
    "projectId": 1,
    "projectTitle": "exact project title from list above",
    "matchScore": 0.95,
    "reason": "Specific reason mentioning student's grades and strengths${userPrefs.interests.map(_ => ", and aligning with your stated interests").getOrElse("")}. For example: 'Your excellent grade in Artificial Intelligence (A, 88%) demonstrates strong expertise needed for this project.'"
  }
]

CRITICAL RULES:
- Use ONLY project IDs and titles from the list above
- Each of the 3 recommendations MUST be a DIFFERENT project (different ID and title)
- Match scores should be between 0.60 and 0.98 (be realistic and varied)
- Reasons must be specific and reference actual student grades/subjects
- Prioritize diversity - avoid recommending similar or overlapping projects
${if (student.cgpa <= 0.0) "- Do NOT mention CGPA in your reasons since it's not available" else ""}
${userPrefs.interests.map(_ => "- Explicitly mention how the project aligns with the student's stated interests").getOrElse("")}
- Return ONLY the JSON array, no other text

Randomization seed: ${randomSeed} (use this to provide variety in your selections)"""
  }
  
  private def buildPreferencesSection(prefs: UserPreferences): String = {
    val sections = scala.collection.mutable.ListBuffer[String]()
    
    prefs.interests.foreach(i => 
      sections += s"Student's Specific Interests: $i"
    )
    
    prefs.preferredDifficulty.foreach(d => 
      sections += s"Preferred Difficulty Level: $d"
    )
    
    prefs.avoidTopics.foreach(a => 
      sections += s"Topics to Avoid: $a"
    )
    
    prefs.additionalNotes.foreach(n => 
      sections += s"Additional Notes: $n"
    )
    
    if (sections.nonEmpty) {
      s"""
Student's Personal Preferences:
${sections.map(s => s"- $s").mkString("\n")}
"""
    } else {
      ""
    }
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
      "temperature": 0.8,
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
      
      // Verify uniqueness
      val uniqueIds = recommendations.map(_.projectId).distinct
      if (uniqueIds.size != recommendations.size) {
        println("WARNING: AI returned duplicate projects, removing duplicates...")
        recommendations.toList.distinctBy(_.projectId).take(3)
      } else {
        recommendations.toList.take(3)
      }
      
    } catch {
      case e: Exception =>
        println(s"Error parsing recommendations: ${e.getMessage}")
        e.printStackTrace()
        getRuleBasedRecommendations(student, projects, List.empty, UserPreferences(None, None, None, None))
    }
  }
  
  /**
   * Fallback: Rule-based recommendation algorithm (no API needed)
   */
  private def getRuleBasedRecommendations(
    student: StudentProfile,
    projects: List[Project],
    excludeProjectIds: List[Int] = List.empty,
    userPrefs: UserPreferences = UserPreferences(None, None, None, None)
  ): List[ProjectRecommendation] = {
    
    println("Using rule-based recommendation algorithm")
    
    // Filter out excluded projects and apply user preferences
    var availableProjects = projects.filterNot(p => excludeProjectIds.contains(p.id))
    
    // Filter by avoid topics
    userPrefs.avoidTopics.foreach { avoid =>
      val avoidKeywords = avoid.toLowerCase.split(",").map(_.trim)
      availableProjects = availableProjects.filterNot(p =>
        avoidKeywords.exists(keyword =>
          p.title.toLowerCase.contains(keyword) ||
          p.description.toLowerCase.contains(keyword) ||
          p.requiredSkills.exists(_.toLowerCase.contains(keyword))
        )
      )
    }
    
    val scoredProjects = availableProjects.map { project =>
      var score = 0.0
      val reasons = scala.collection.mutable.ListBuffer[String]()
      
      // Add some randomness to get different results each time
      val randomBonus = Random.nextDouble() * 0.1
      score += randomBonus
      
      // 1. Difficulty matching (30% weight)
      val difficultyMatch = userPrefs.preferredDifficulty match {
        case Some(prefDiff) =>
          // User has explicit preference
          if (project.difficultyLevel.equalsIgnoreCase(prefDiff)) {
            reasons += s"Matches your preferred ${prefDiff.toLowerCase} difficulty level"
            0.3
          } else {
            0.1
          }
        case None if student.cgpa > 0.0 =>
          // Use CGPA if available
          project.difficultyLevel.toLowerCase match {
            case "beginner" if student.cgpa < 3.0 => 0.3
            case "intermediate" if student.cgpa >= 3.0 && student.cgpa < 3.6 => 0.3
            case "advanced" if student.cgpa >= 3.6 => 0.3
            case "intermediate" if student.cgpa >= 2.5 => 0.2
            case _ => 0.1
          }
        case None =>
          // Use average subject performance
          val avgPercentage = if (student.topSubjects.nonEmpty) {
            student.topSubjects.map(_.percentage).sum / student.topSubjects.size
          } else {
            70.0
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
      
      // 2. Subject/skill matching (40% weight)
      val subjectNames = student.topSubjects.map(_.subjectName.toLowerCase).toSet
      val matchingSkills = project.requiredSkills.filter(skill =>
        subjectNames.exists(subj => 
          subj.contains(skill.toLowerCase) || skill.toLowerCase.contains(subj)
        )
      )
      
      if (matchingSkills.nonEmpty) {
        val skillScore = (matchingSkills.size.toDouble / project.requiredSkills.size) * 0.4
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
      
      // 3. User interests matching (30% weight)
      val allInterests = List(
        Option(student.interests).filter(_.nonEmpty),
        userPrefs.interests
      ).flatten.mkString(", ")
      
      if (allInterests.nonEmpty) {
        val interests = allInterests.toLowerCase.split(",").map(_.trim)
        val interestMatches = interests.count(interest =>
          project.title.toLowerCase.contains(interest) ||
          project.description.toLowerCase.contains(interest) ||
          project.requiredSkills.exists(_.toLowerCase.contains(interest))
        )
        
        if (interestMatches > 0) {
          score += 0.3
          reasons += s"Aligns with your interests in ${allInterests}"
        }
      }
      
      val finalReason = if (reasons.isEmpty) {
        "Good match for your academic profile"
      } else {
        reasons.mkString(". ")
      }
      
      ProjectRecommendation(project.id, project.title, score, finalReason)
    }
    
    // Shuffle to add more variety, then sort by score
    Random.shuffle(scoredProjects).sortBy(rec => -rec.matchScore).take(3)
  }
}