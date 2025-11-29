package org.example.models

import org.springframework.data.cassandra.core.mapping.{PrimaryKey, Table, Column}
import scala.beans.BeanProperty

@Table("subjects")
class RealSubject {
  
  @PrimaryKey
  @BeanProperty
  var id: Int = _
  
  @Column("programmecode")
  @BeanProperty
  var programmeCode: String = _
  
  @Column("subjectcode")
  @BeanProperty
  var subjectCode: String = _
  
  @Column("subjectname")
  @BeanProperty
  var subjectName: String = _
  
  @Column("examyear")
  @BeanProperty
  var examYear: Int = _
  
  @Column("exammonth")
  @BeanProperty
  var examMonth: String = _
  
  @BeanProperty
  var status: String = _
  
  @BeanProperty
  var grade: String = _
  
  @Column("overallpercentage")
  @BeanProperty
  var overallPercentage: java.lang.Double = _
  
  @Column("exampercentage")
  @BeanProperty
  var examPercentage: java.lang.Double = _
  
  @Column("courseworkpercentage")
  @BeanProperty
  var courseworkPercentage: java.lang.Double = _
}