package utils

import scala.collection.immutable.HashMap

class ReportReturnData(fileName:String, fileterKey:String) {
  val reportReturnData:HashMap = JsonStore.LoadData("resources/reply/reply.json", "report")


}
