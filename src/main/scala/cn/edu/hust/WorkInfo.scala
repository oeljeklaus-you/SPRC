package cn.edu.hust

class WorkInfo(val id:String,val memory:Int, val cores:Int) {
  //存储上一次心跳通信的时间
  var Pre_Time:Long=_
}
