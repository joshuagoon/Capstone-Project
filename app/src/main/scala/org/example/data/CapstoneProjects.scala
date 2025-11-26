package org.example.data

case class Project(
  id: Int,
  title: String,
  description: String,
  difficultyLevel: String,
  requiredSkills: List[String],
  supervisor: String
)

object CapstoneProjects {
  
  val projects: List[Project] = List(
    Project(
      1,
      "AI-Powered Student Chatbot",
      "Develop an intelligent chatbot using Natural Language Processing to provide 24/7 student support for academic queries, course information, and campus services.",
      "Advanced",
      List("Artificial Intelligence", "Natural Language Processing", "Python", "Machine Learning"),
      "Dr. Chin Teck Min"
    ),
    
    Project(
      2,
      "Learning Analytics Dashboard",
      "Build a real-time performance tracking and visualization system that helps students and educators monitor academic progress, identify learning gaps, and predict outcomes.",
      "Intermediate",
      List("Web Development", "Data Visualization", "Database Management", "Statistics"),
      "Dr. Wong Mei Ling"
    ),
    
    Project(
      3,
      "Mobile Study Planner App",
      "Create a cross-platform mobile application for academic planning, including timetable management, assignment tracking, and study session scheduling with reminder notifications.",
      "Intermediate",
      List("Mobile Development", "React Native", "Database Management", "UI/UX Design"),
      "Dr. Tan Chee Keong"
    ),
    
    Project(
      4,
      "E-Learning Platform with Video Streaming",
      "Develop an interactive online learning platform featuring live video streaming, recorded lectures, interactive quizzes, and student discussion forums.",
      "Advanced",
      List("Web Development", "Cloud Computing", "Video Streaming", "Database Management"),
      "Dr. Lee Siew Eng"
    ),
    
    Project(
      5,
      "Blockchain-Based Certificate Verification System",
      "Build a tamper-proof digital certificate verification system using blockchain technology to prevent academic credential fraud and enable instant verification.",
      "Advanced",
      List("Blockchain", "Cryptography", "Web Development", "Smart Contracts"),
      "Dr. Kumar Rajesh"
    ),
    
    Project(
      6,
      "IoT Smart Campus Management System",
      "Create an IoT-based system for campus facility management including smart lighting, temperature control, occupancy tracking, and energy monitoring.",
      "Advanced",
      List("Internet of Things", "Sensor Networks", "Cloud Computing", "Mobile Development"),
      "Dr. Rahman Abdullah"
    ),
    
    Project(
      7,
      "AI-Based Automated Timetable Scheduler",
      "Develop an intelligent system that automatically generates optimal class schedules considering room availability, lecturer preferences, student conflicts, and resource constraints.",
      "Intermediate",
      List("Artificial Intelligence", "Algorithms", "Database Management", "Optimization"),
      "Dr. Chen Wei"
    ),
    
    Project(
      8,
      "Virtual Laboratory Simulator",
      "Build a 3D simulation environment for conducting virtual science experiments with realistic physics, chemical reactions, and equipment interactions.",
      "Advanced",
      List("3D Graphics", "Physics Engine", "Web Development", "Game Development"),
      "Dr. Singh Harpreet"
    ),
    
    Project(
      9,
      "Student Mental Health Support Mobile App",
      "Design a mobile application for mental health tracking, mood monitoring, stress management resources, and connection to counseling services.",
      "Beginner",
      List("Mobile Development", "UI/UX Design", "Database Management", "Psychology Basics"),
      "Dr. Ahmad Zainuddin"
    ),
    
    Project(
      10,
      "AI-Powered Plagiarism Detection System",
      "Create an advanced plagiarism detection system using machine learning and natural language processing to identify copied content, paraphrased text, and code similarity.",
      "Advanced",
      List("Artificial Intelligence", "Natural Language Processing", "Machine Learning", "Web Development"),
      "Dr. Lim Hui Ling"
    ),
    
    Project(
      11,
      "Student Portal and Information System",
      "Develop a comprehensive web-based student portal for course registration, grade viewing, fee payment, document requests, and academic announcements.",
      "Intermediate",
      List("Web Development", "Database Management", "Security", "UI/UX Design"),
      "Dr. Tan Soo Ling"
    ),
    
    Project(
      12,
      "Augmented Reality Campus Navigation App",
      "Build a mobile app using augmented reality to help students navigate campus, find classrooms, locate facilities, and discover campus events.",
      "Advanced",
      List("Augmented Reality", "Mobile Development", "Computer Vision", "3D Modeling"),
      "Dr. Yusof Hashim"
    )
  )
  
  def getAll: List[Project] = projects
  
  def getById(id: Int): Option[Project] = projects.find(_.id == id)
  
  def getByDifficulty(level: String): List[Project] = 
    projects.filter(_.difficultyLevel.equalsIgnoreCase(level))
  
  def searchBySkill(skill: String): List[Project] = 
    projects.filter(p => p.requiredSkills.exists(_.toLowerCase.contains(skill.toLowerCase)))
  
  // Convert to titles list for AI prompt
  def getTitlesWithDetails: List[String] = 
    projects.map(p => 
      s"${p.id}. ${p.title} (${p.difficultyLevel}) - ${p.description}"
    )
}