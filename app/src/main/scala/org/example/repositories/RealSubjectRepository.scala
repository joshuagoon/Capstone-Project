package org.example.repositories

import org.example.models.RealSubject
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.data.cassandra.repository.Query
import org.springframework.stereotype.Repository

@Repository
trait RealSubjectRepository extends CassandraRepository[RealSubject, Int] {
  
  // Find subjects by student ID (from subjects field mapping)
  // Note: This might need custom implementation based on your data structure
}