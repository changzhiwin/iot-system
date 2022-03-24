package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceGroupSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  "DeviceGroup actor" must {
    "get all temperature in once query" in {
      val groupName = "group-1"
      val deviceGroupActor = spawn(DeviceGroup(groupName))
      val probe = createTestProbe[DeviceManager.DeviceRegistered]()

      // register device 1
      deviceGroupActor ! DeviceManager.RequestTrackDevice(groupName, "device-1", probe.ref)
      val device1 = probe.receiveMessage()
      // record temperature
      val device1Probe = createTestProbe[Device.TemperatureRecorded]()
      device1.device ! Device.RecordTemperature(1, 35.6, device1Probe.ref)
      val ret1 = device1Probe.receiveMessage()
      ret1.requestId should ===(1)

      // register device 2
      deviceGroupActor ! DeviceManager.RequestTrackDevice(groupName, "device-2", probe.ref)
      val device2 = probe.receiveMessage()
      // record temperature
      val device2Probe = createTestProbe[Device.TemperatureRecorded]()
      device2.device ! Device.RecordTemperature(2, 37.2, device2Probe.ref)
      val ret2 = device2Probe.receiveMessage()
      ret2.requestId should ===(2)

      // get all temperature
      val getAllProbe = createTestProbe[DeviceManager.RespondAllTemperatures]()
      deviceGroupActor ! DeviceManager.RequestAllTemperatures(3, groupName, getAllProbe.ref)
      val allTemperatures = getAllProbe.receiveMessage()
      allTemperatures.requestId should ===(3)
      allTemperatures.temperatures should ===(Map("device-1" -> DeviceManager.Temperature(35.6), "device-2" -> DeviceManager.Temperature(37.2)))
    }
  }
}