package org.example.repositories

import org.example.models.RealStudent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository

@Repository
trait RealStudentRepository extends CassandraRepository[RealStudent, Int] {
  // Spring Data will auto-implement this
}