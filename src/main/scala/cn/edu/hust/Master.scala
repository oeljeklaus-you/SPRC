package cn.edu.hust

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import scala.concurrent.duration._
import scala.collection.mutable

class Master(val  host:String,val port:Int) extends Actor{
  val works=new mutable.HashMap[String,WorkInfo]()
  val works_avaliable=new mutable.HashSet[WorkInfo]()
  val heartBeat_interval:Long=15000
  //在构造方法之后，receive方法之前启动
  override def preStart(): Unit = {
      println("start method invoked")
      import context.dispatcher
      context.system.scheduler.schedule(0 millis ,heartBeat_interval millis,self,CheckHeartBeat)
  }

  def func(worker:WorkInfo):Boolean={
    System.currentTimeMillis()-worker.Pre_Time>heartBeat_interval
  }

  override def receive():Receive = {
    //worker向服务端注册信息
    case RegisterWorkInfo(id,memory,cores) =>{
      //如果没有包含这个worker，那么就将这个worker的信息保存在本地，然后发送信息
      if(!works.contains(id))
        {
          val worker_new=new WorkInfo(id,memory,cores)
          works.put(id,worker_new)
          works_avaliable+=worker_new
        }
      //发送消息给worker,如果注册成功返回
      sender ! RegisterResponse(s"akka.tcp://system@$host:$port/user/Master")
    }

    case HeartBeatToMaster(id) =>
    {
      if(works.contains(id))
        works(id).Pre_Time=System.currentTimeMillis()
      //works_avaliable.
        //sender() ! "ok"
    }

    case CheckHeartBeat=>
    {
        //val func=(worker:WorkInfo)=>Boolean {}

       val toRemove=works_avaliable.filter(func)
       for(info <- toRemove) {
          works -= info.id
          works_avaliable -= info
       }
       println(works.size)
    }
  }
}
object Master {
  def main(args: Array[String]): Unit = {
    val host=args(0)
    val port=args(1).toInt
    val conf=s"""|akka.actor.provider = "akka.remote.RemoteActorRefProvider"
                    |akka.remote.netty.tcp.hostname = "$host"
                    |akka.remote.netty.tcp.port = "$port"
                 """.stripMargin
     val config=ConfigFactory.parseString(conf)
    //1.创建一个ActorSystem用于监控和管理所有的actor
     val actorSystem=ActorSystem.create("system",config)
    //创建一个actor，这里表示Master
     val master=actorSystem.actorOf(Props(new Master(host,port)), "Master")
      //发送异步消息
     //master!"connect"
    //退出
    actorSystem.awaitTermination()
  }
}
