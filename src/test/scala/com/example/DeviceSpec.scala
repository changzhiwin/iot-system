package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import Device._

  "Device actor" must {
    "reply with empty reading if no temperature is known" in {
      val probe = createTestProbe[RespondTemperature]()
      val deviceActor = spawn(Device("group", "device"))

      deviceActor ! ReadTemperature(requestId = 33, probe.ref)
      val response = probe.receiveMessage()
      response.requestId should ===(33)
      response.value should ===(None)
    }

    "record temperatrue and answer correct" in {
      val recordProbe = createTestProbe[TemperatureRecorded]()
      val deviceActor = spawn(Device("group", "device"))

      // for record
      deviceActor ! RecordTemperature(34, 23.45, recordProbe.ref)
      recordProbe.expectMessage(TemperatureRecorded(34))

      // for query 
      val queryProbe = createTestProbe[RespondTemperature]()
      deviceActor ! ReadTemperature(requestId = 35, queryProbe.ref)
      val queryRet = queryProbe.receiveMessage()
      queryRet.requestId should ===(35)
      queryRet.value should ===(Some(23.45))
    }
  }
}