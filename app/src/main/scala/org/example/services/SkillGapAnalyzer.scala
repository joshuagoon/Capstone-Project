package org.example.services

import org.springframework.stereotype.Service
import scala.beans.BeanProperty

// Data classes for skill gap analysis
case class SkillGap(
  skillName: String,
  currentScore: Double,
  requiredScore: Double,
  gap: Double,
  status: String,
  recommendation: String,
  relatedCourses: List[String] // NEW: List of actual courses
)

case class GapAnalysisResult(
  projectTitle: String,
  projectDifficulty: String,
  overallReadiness: Double,
  strongAreas: List[SkillGap],
  areasToImprove: List[SkillGap],
  estimatedPrepTime: String,
  readinessLevel: String
)

case class StudentSubject(
  courseName: String,
  grade: String,
  percentage: Double
)

@Service
class SkillGapAnalyzer {

  // Define minimum required scores for different difficulty levels
  private val requiredScoresByDifficulty = Map(
    "Beginner" -> 60.0,
    "Intermediate" -> 70.0,
    "Advanced" -> 80.0
  )

  // Skill keyword mappings to match project requirements with student competencies
  private val skillMappings = Map(
    "artificial intelligence" -> List(
        "artificial intelligence",
        "artificialintelligence",
        "ai",
        "machine learning",
        "machinelearning",
        "ml",
        "computational intelligence", 
        "computationalintelligence",
        "computer vision", 
        "computervision",
        "neural"
    ),
    
    "machine learning" -> List(
        "artificial intelligence", 
        "artificialintelligence",
        "machine learning", 
        "machinelearning",
        "ml", 
        "ai", 
        "computational intelligence",
        "computationalintelligence"
    ),
    
    "web development" -> List(
        "web", 
        "html", 
        "javascript", 
        "react", 
        "frontend", 
        "backend", 
        "internet", 
        "application development",
        "applicationdevelopment"
    ),
    
    "mobile development" -> List(
        "mobile", 
        "android", 
        "ios", 
        "app development", 
        "appdevelopment",
        "application development",
        "applicationdevelopment"
    ),
    
    "data science" -> List(
        "data science", 
        "datascience",
        "analytics", 
        "statistics", 
        "data mining", 
        "datamining",
        "data", 
        "business intelligence",
        "businessintelligence"
    ),
    
    "data analysis" -> List(
        "data science", 
        "datascience",
        "analytics", 
        "data analysis", 
        "dataanalysis",
        "data mining", 
        "datamining",
        "data", 
        "visual analytics",
        "visualanalytics"
    ),
    
    "database" -> List(
        "database", 
        "sql", 
        "data management", 
        "datamanagement",
        "information systems",
        "informationsystems", 
        "database management",
        "databasemanagement"
    ),
    
    "databases" -> List(
        "database", 
        "sql", 
        "data management", 
        "datamanagement",
        "information systems",
        "informationsystems", 
        "database management",
        "databasemanagement"
    ),
    
    "cybersecurity" -> List(
        "security", 
        "cyber", 
        "cryptography", 
        "hacking", 
        "cybersecurity", 
        "network security",
        "networksecurity", 
        "forensic"
    ),
    
    "security" -> List(
        "security", 
        "cyber", 
        "cryptography", 
        "hacking", 
        "cybersecurity", 
        "network security",
        "networksecurity"
    ),
    
    "networks" -> List(
        "network", 
        "networks", 
        "communication", 
        "distributed", 
        "data communication",
        "datacommunication",
        "distributedsystems"
    ),
    
    "networking" -> List(
        "network", 
        "networks", 
        "communication", 
        "distributed", 
        "data communication",
        "datacommunication"
    ),
    
    "software engineering" -> List(
        "software engineering", 
        "softwareengineering",
        "software", 
        "architecture", 
        "design pattern", 
        "designpattern",
        "software process", 
        "softwareprocess",
        "requirement"
    ),
    
    "programming" -> List(
        "programming", 
        "code", 
        "software development", 
        "softwaredevelopment",
        "object-oriented",
        "objectoriented", 
        "software", 
        "problem solving",
        "problemsolving"
    ),
    
    "ui/ux" -> List(
        "ui/ux", 
        "uiux",
        "user interface", 
        "userinterface",
        "design", 
        "frontend", 
        "web", 
        "human computer interaction",
        "humancomputerinteraction"
    ),
    
    "cloud computing" -> List(
        "cloud", 
        "aws", 
        "azure", 
        "distributed systems",
        "distributedsystems",
        "cloudcomputing"
    ),
    
    "iot" -> List(
        "iot", 
        "internet of things", 
        "internetofthings",
        "embedded", 
        "sensors", 
        "digital systems",
        "digitalsystems", 
        "electronics"
    ),
    
    "blockchain" -> List(
        "blockchain", 
        "smart contracts", 
        "smartcontracts",
        "distributed ledger", 
        "distributedledger",
        "cryptography"
    )
    )

  /**
   * Main method to analyze skill gaps WITH student subject data
   */
  def analyzeSkillGapWithSubjects(
    studentCompetencies: Map[String, Double],
    studentSubjects: List[StudentSubject],
    projectTitle: String,
    projectSkills: List[String],
    projectDifficulty: String
  ): GapAnalysisResult = {
    
    val requiredScore = requiredScoresByDifficulty.getOrElse(projectDifficulty, 70.0)
    
    println(s"Analyzing skill gap for project: $projectTitle")
    println(s"Required skills: ${projectSkills.mkString(", ")}")
    println(s"Student has ${studentSubjects.size} courses")
    
    // Analyze each required skill
    val skillAnalysis = projectSkills.map { skill =>
      val currentScore = findMatchingCompetency(studentCompetencies, skill)
      val gap = math.max(0, requiredScore - currentScore)
      
      // Find related courses for this skill
      val relatedCourses = findRelatedCourses(skill, studentSubjects)
      
      val status = determineStatus(gap)
      val recommendation = generateRecommendationWithCourses(
        skill, 
        gap, 
        projectDifficulty,
        relatedCourses
      )
      
      SkillGap(
        skillName = skill,
        currentScore = currentScore,
        requiredScore = requiredScore,
        gap = gap,
        status = status,
        recommendation = recommendation,
        relatedCourses = relatedCourses.map(c => s"${c.courseName} (${c.grade}, ${c.percentage.toInt}%)").take(3)
      )
    }
    
    // Separate strong areas from areas needing improvement
    val strongAreas = skillAnalysis.filter(_.gap == 0).sortBy(-_.currentScore)
    val areasToImprove = skillAnalysis.filter(_.gap > 0).sortBy(-_.gap)
    
    // Calculate overall metrics
    val overallReadiness = calculateReadiness(skillAnalysis)
    val prepTime = estimatePrepTime(areasToImprove)
    val readinessLevel = determineReadinessLevel(overallReadiness)
    
    GapAnalysisResult(
      projectTitle = projectTitle,
      projectDifficulty = projectDifficulty,
      overallReadiness = math.round(overallReadiness * 10) / 10.0,
      strongAreas = strongAreas,
      areasToImprove = areasToImprove,
      estimatedPrepTime = prepTime,
      readinessLevel = readinessLevel
    )
  }
  
  /**
   * Find courses related to a specific skill
   */
  private def findRelatedCourses(
    skill: String,
    subjects: List[StudentSubject]
    ): List[StudentSubject] = {
    
    val normalizedSkill = skill.toLowerCase.trim
    val keywords = skillMappings.getOrElse(normalizedSkill, List(normalizedSkill))
    
    println(s"\n🔍 Finding courses for skill: '$skill'")
    println(s"   Normalized skill: '$normalizedSkill'")
    println(s"   Keywords to match: ${keywords.mkString(", ")}")
    println(s"   Total subjects to search: ${subjects.size}")
    
    val matchedCourses = subjects.filter { subject =>
        val courseName = subject.courseName.toLowerCase.trim
        
        // Remove all spaces and special characters for fuzzy matching
        val courseNameNoSpaces = courseName.replaceAll("[\\s-_]+", "")
        
        println(s"   Checking course: '$courseName' (normalized: '$courseNameNoSpaces')")
        
        val matches = keywords.exists { keyword =>
        val keywordNoSpaces = keyword.replaceAll("[\\s-_]+", "")
        
        // Try multiple matching strategies
        val exactMatch = courseName.contains(keyword)
        val noSpaceMatch = courseNameNoSpaces.contains(keywordNoSpaces)
        val reverseMatch = keyword.contains(courseName)
        val partialMatch = courseName.split("\\s+").exists(word => 
            keyword.split("\\s+").exists(kw => word.startsWith(kw) || kw.startsWith(word))
        )
        
        val matched = exactMatch || noSpaceMatch || reverseMatch || partialMatch
        
        if (matched) {
            println(s"      ✅ MATCHED with keyword '$keyword'")
        }
        
        matched
        }
        
        matches
    }.sortBy(-_.percentage)
    
    println(s"   ✅ Found ${matchedCourses.size} matching courses:")
    matchedCourses.foreach(c => 
        println(s"      - ${c.courseName}: ${c.grade} (${c.percentage.toInt}%)")
    )
    
    matchedCourses
    }
  
  /**
   * Generate personalized recommendations based on actual courses
   */
  private def generateRecommendationWithCourses(
    skill: String,
    gap: Double,
    difficulty: String,
    relatedCourses: List[StudentSubject]
  ): String = {
    
    if (gap == 0) {
      if (relatedCourses.nonEmpty) {
        val bestCourse = relatedCourses.head
        s"✅ Excellent! Your strong performance in ${bestCourse.courseName} (${bestCourse.grade}, ${bestCourse.percentage.toInt}%) shows you're well-prepared in $skill for this $difficulty project."
      } else {
        s"✅ Excellent! You're well-prepared in $skill for this $difficulty project."
      }
    } else if (relatedCourses.isEmpty) {
      // No related courses found - student hasn't taken them yet
      s"⚠️ You haven't taken courses specifically focused on $skill. Consider taking relevant courses or self-study through online resources before attempting this $difficulty project."
    } else {
      // They have courses - check performance
      val avgPerformance = relatedCourses.map(_.percentage).sum / relatedCourses.size
      val lowScoreCourses = relatedCourses.filter(_.percentage < 70.0)
      
      if (gap <= 5) {
        if (lowScoreCourses.isEmpty) {
          val topCourse = relatedCourses.head
          s"⚡ Almost there! Your performance in ${topCourse.courseName} (${topCourse.grade}) is solid. Review key concepts and complete one practice project to strengthen your $skill abilities."
        } else {
          val weakCourse = lowScoreCourses.head
          s"⚡ Nearly ready! Review concepts from ${weakCourse.courseName} where you scored ${weakCourse.percentage.toInt}%. Complete a practice project to solidify your understanding."
        }
      } else if (gap <= 15) {
        if (lowScoreCourses.size >= 2) {
          val courseNames = lowScoreCourses.take(2).map(c => s"${c.courseName} (${c.grade})").mkString(" and ")
          s"📚 Your performance in $courseNames indicates this area needs strengthening. Review these course materials thoroughly and complete 2-3 hands-on projects to improve your $skill abilities."
        } else if (lowScoreCourses.size == 1) {
          val weakCourse = lowScoreCourses.head
          s"📚 Focus on improving your understanding of concepts from ${weakCourse.courseName} where you scored ${weakCourse.percentage.toInt}%. Review notes and complete 2-3 practice projects."
        } else {
          s"📚 While you've taken $skill courses, more practice is needed. Complete 2-3 hands-on projects to build confidence and practical skills."
        }
      } else {
        // Significant gap
        if (lowScoreCourses.nonEmpty) {
          val weakCourses = lowScoreCourses.take(2).map(c => s"${c.courseName} (${c.percentage.toInt}%)").mkString(", ")
          s"⚠️ Significant gap detected. Your scores in $weakCourses suggest fundamental concepts need reinforcement. Consider retaking or auditing these courses, and dedicate 4-6 weeks to focused study with practical projects."
        } else {
          s"⚠️ Significant gap detected. While you've taken $skill courses, you need deeper understanding. Dedicate 4-6 weeks to intensive study, review course materials, and complete multiple real-world projects."
        }
      }
    }
  }
  
  /**
   * Find the best matching competency score for a given skill
   */
  private def findMatchingCompetency(
    competencies: Map[String, Double], 
    requiredSkill: String
  ): Double = {
    
    val normalizedSkill = requiredSkill.toLowerCase.trim
    
    println(s"🔍 Looking for match for: '$requiredSkill'")
    println(s"   Available competencies: ${competencies.keys.mkString(", ")}")
    
    // First, try direct match
    val directMatch = competencies.find { case (compName, _) =>
      compName.toLowerCase == normalizedSkill
    }
    
    if (directMatch.isDefined) {
      println(s"   ✅ Direct match found: ${directMatch.get._1} = ${directMatch.get._2}")
      return directMatch.get._2
    }
    
    // Second, use skill mappings for fuzzy matching
    val keywords = skillMappings.getOrElse(normalizedSkill, List(normalizedSkill))
    println(s"   🔎 Trying keywords: ${keywords.mkString(", ")}")
    
    val matchedCompetency = competencies.find { case (compName, _) =>
      val compNameLower = compName.toLowerCase
      keywords.exists(kw => 
        compNameLower.contains(kw) || kw.contains(compNameLower)
      )
    }
    
    if (matchedCompetency.isDefined) {
      println(s"   ✅ Keyword match found: ${matchedCompetency.get._1} = ${matchedCompetency.get._2}")
      return matchedCompetency.get._2
    }
    
    // Third, try partial word matching (more aggressive)
    val partialMatch = competencies.find { case (compName, _) =>
      val compWords = compName.toLowerCase.split("\\s+|&").filter(_.length > 3)
      val skillWords = normalizedSkill.split("\\s+").filter(_.length > 3)
      
      compWords.exists(cw => skillWords.exists(sw => 
        (cw.contains(sw) && sw.length > 3) || (sw.contains(cw) && cw.length > 3)
      ))
    }
    
    if (partialMatch.isDefined) {
      println(s"   ✅ Partial match found: ${partialMatch.get._1} = ${partialMatch.get._2}")
      return partialMatch.get._2
    }
    
    println(s"   ⚠️ No match found for '$requiredSkill', using default score 50.0")
    50.0
  }
  
  /**
   * Determine status based on gap size
   */
  private def determineStatus(gap: Double): String = {
    if (gap == 0) "Ready"
    else if (gap <= 5) "Nearly Ready"
    else if (gap <= 15) "Needs Improvement"
    else "Significant Gap"
  }
  
  /**
   * Calculate overall readiness percentage
   */
  private def calculateReadiness(analysis: List[SkillGap]): Double = {
    if (analysis.isEmpty) return 100.0
    
    val totalCurrentScore = analysis.map(_.currentScore).sum
    val totalRequiredScore = analysis.map(_.requiredScore).sum
    
    if (totalRequiredScore == 0) 100.0
    else (totalCurrentScore / totalRequiredScore) * 100.0
  }
  
  /**
   * Estimate preparation time based on gaps
   */
  private def estimatePrepTime(gaps: List[SkillGap]): String = {
    if (gaps.isEmpty) return "Ready Now ✅"
    
    val totalGap = gaps.map(_.gap).sum
    val avgGap = totalGap / gaps.size
    val skillCount = gaps.size
    
    val complexityFactor = (avgGap * skillCount) / 10
    
    if (complexityFactor <= 2) "1-2 weeks"
    else if (complexityFactor <= 5) "3-4 weeks"
    else if (complexityFactor <= 8) "6-8 weeks"
    else if (complexityFactor <= 12) "2-3 months"
    else "3-4 months (Consider a different project)"
  }
  
  /**
   * Determine readiness level category
   */
  private def determineReadinessLevel(readiness: Double): String = {
    if (readiness >= 90) "Highly Ready"
    else if (readiness >= 75) "Ready with Minor Prep"
    else if (readiness >= 60) "Moderate Preparation Needed"
    else if (readiness >= 40) "Significant Preparation Needed"
    else "Not Recommended - Consider Another Project"
  }
}