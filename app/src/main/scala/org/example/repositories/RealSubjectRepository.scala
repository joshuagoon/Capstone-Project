package org.example.repositories

import org.springframework.data.cassandra.repository.{CassandraRepository, Query}
import org.springframework.stereotype.Repository
import org.example.models.RealSubject

@Repository
trait RealSubjectRepository extends CassandraRepository[RealSubject, Int] {
  
  @Query("SELECT * FROM subjects WHERE id = ?0 ALLOW FILTERING")
  def findByStudentId(studentId: Int): java.util.List[RealSubject]
}