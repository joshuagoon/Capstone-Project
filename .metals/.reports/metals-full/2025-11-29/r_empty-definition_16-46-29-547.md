error id: file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/models/RealStudent.scala:`<none>`.
file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/models/RealStudent.scala
empty definition using pc, found symbol in pc: `<none>`.
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -org/springframework/data/cassandra/core/mapping/PrimaryKey#
	 -PrimaryKey#
	 -scala/Predef.PrimaryKey#
offset: 197
uri: file:///C:/Users/Joshua%20Goon/OneDrive/Documents/GitHub/Capstone-Project/app/src/main/scala/org/example/models/RealStudent.scala
text:
```scala
package org.example.models

import org.springframework.data.cassandra.core.mapping.{PrimaryKey, Table, Column}
import scala.beans.BeanProperty

@Table("students")
class RealStudent {
  
  @@@PrimaryKey
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
  
  @BeanProperty
  var subjects: String = _ // JSON string or comma-separated
  
  @Column("yearonecgpa")
  @BeanProperty
  var yearOneCgpa: java.lang.Double = _
  
  def this() = this
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: `<none>`.