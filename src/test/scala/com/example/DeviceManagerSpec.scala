package com.example

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class DeviceManagerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "DeviceManager actor" must {
    "register 3 devices in one group, then one down" in {
      
      val deviceManagerActor = spawn(DeviceManager())

      val probe = createTestProbe[DeviceManager.DeviceRegistered]()
      deviceManagerActor ! DeviceManager.RequestTrackDevice("group-1", "device-1", probe.ref)
      probe.receiveMessage()

      deviceManagerActor ! DeviceManager.RequestTrackDevice("group-1", "device-2", probe.ref)
      probe.receiveMessage()

      deviceManagerActor ! DeviceManager.RequestTrackDevice("group-1", "device-3", probe.ref)
      val device3 = probe.receiveMessage()

      val deviceListProbe = createTestProbe[DeviceManager.ReplyDeviceList]()
      deviceManagerActor ! DeviceManager.RequestDeviceList(1, "group-1", deviceListProbe.ref)
      val deviceListResp = deviceListProbe.receiveMessage()

      deviceListResp.requestId should ===(1)
      deviceListResp.ids should ===(Set("device-1", "device-2", "device-3"))

      device3.device ! Device.Passivate
      // probe.expectTerminated(device3.device, probe.remainingOrDefault)

      deviceListProbe.awaitAssert {
        deviceManagerActor ! DeviceManager.RequestDeviceList(2, "group-1", deviceListProbe.ref)
        deviceListProbe.expectMessage(DeviceManager.ReplyDeviceList(2, Set("device-1", "device-2")))
      }
    }
  }
}