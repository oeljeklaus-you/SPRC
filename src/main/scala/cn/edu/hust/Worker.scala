package cn.edu.hust

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

class Worker(val masterHost:String,val masterPort:Int) extends Actor{
  var master:ActorSelection= _
  //给每一个worker分配一个唯一的id
  val id=UUID.randomUUID().toString
  val beats=10000
  override def preStart(): Unit = {
    //1.连接server，获取master实例
    master=context.actorSelection(s"akka.tcp://system@$masterHost:$masterPort/user/Master")
    //2.使用master实例注册消息
    master! RegisterWorkInfo(id,10240,4)

  }

  override def receive ():Receive = {
    case RegisterResponse(url)=>{
      println(s"the master work in $url")
      import context.dispatcher
      //配置一个定时调度器，定时向master发送任务
      context.system.scheduler.schedule(0 millis ,beats millis,self,HeartBeat)
    }
    case HeartBeat=>{
      master!HeartBeatToMaster(id)
    }
  }
}
object Worker
{
  def main(args: Array[String]): Unit = {
      val masterHost=args(0)
      val masterPort=args(1).toInt
      val workerHost=args(2)
      val workerPort=args(3).toInt
      val conf=s"""|akka.actor.provider = "akka.remote.RemoteActorRefProvider"
                   |akka.remote.netty.tcp.hostname = "$workerHost"
                   |akka.remote.netty.tcp.port = "$workerPort"
                 """.stripMargin
      val config=ConfigFactory.parseString(conf)
      val system=ActorSystem.create("worker",config)
      val worker=system.actorOf(Props(new Worker(masterHost,masterPort)),"Worker")
    system.awaitTermination()
  }
}
