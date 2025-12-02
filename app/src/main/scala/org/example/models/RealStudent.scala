package org.example.models

import org.springframework.data.cassandra.core.mapping.{PrimaryKey, Table, Column}
import scala.beans.BeanProperty

@Table("students")
class RealStudent {
  
  @PrimaryKey
  @BeanProperty
  var id: Int = _
  
  @BeanProperty
  var name: String = _
  
  @Column("programmecode")
  @BeanProperty
  var programmeCode: String = _
  
  @Column("programme")
  @BeanProperty
  var programme: String = _
  
  @Column("overallcgpa")
  @BeanProperty
  var overallCgpa: java.lang.Double = _
  
  @Column("overallcavg")
  @BeanProperty
  var overallCavg: java.lang.Double = _
  
  @BeanProperty
  var cohort: Int = _
  
  @BeanProperty
  var graduated: Boolean = _
  
  @Column("yearonecgpa")
  @BeanProperty
  var yearOneCgpa: java.lang.Double = _
}