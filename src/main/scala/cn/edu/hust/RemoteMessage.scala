package cn.edu.hust

trait RemoteMessage extends Serializable
//从worker到master的消息
case class RegisterWorkInfo(id:String,memory:Int,cores:Int) extends RemoteMessage
//master向worker发送消息
case class RegisterResponse(url:String) extends RemoteMessage
//master向worker发送心跳消息
case class HeartBeatToMaster(id:String)extends RemoteMessage
//worker自己向自己发送心跳信息
case object HeartBeat
case object CheckHeartBeat
