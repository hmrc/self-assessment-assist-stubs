import com.fasterxml.jackson.annotation.JsonValue

import scala.collection.immutable.HashMap


object JsonStore {
  def LoadData(fileName:String, fileterKey:String ) : Option[HashMap[String, JsonValue]] = ???
  def get(key:String) : Option[JsonValue] = ???

}