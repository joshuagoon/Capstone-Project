package org.example.repositories

import org.example.models.Student
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository

@Repository
trait StudentRepository extends CassandraRepository[Student, Int] {
  def findByEmail(email: String): java.util.Optional[Student]
}