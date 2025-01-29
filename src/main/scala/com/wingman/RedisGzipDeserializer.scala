package com.wingman

import redis.clients.jedis.Jedis
import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import java.util.zip.GZIPInputStream
import scala.util.Try

object RedisGzipDeserializer {
  def main(args: Array[String]): Unit = {
    val redisKey = "wingman-snapshot:user123"
    val jedis = new Jedis("localhost", 6379) // Connect to Redis

    println(s"Fetching key: $redisKey from Redis")
    val compressedData: Array[Byte] = jedis.get(redisKey.getBytes())

    if (compressedData == null) {
      println("No data found for the given key.")
    } else {
      println(s"Retrieved ${compressedData.length} bytes from Redis")

      val decompressedData = decompress(compressedData)
      println(s"Decompressed data: $decompressedData")
    }

    jedis.close()
  }

  //Decompressed data:       �� sr $com.wingman.persistence.UserSnapshot ]��a��r L datat  Lscala/collection/immutable/Map;xpsr #scala.collection.immutable.Map$Map3�,?��$q L key1t Ljava/lang/Object;L key2q ~ L key3q ~ L value1q ~ L value2q ~ L value3q ~ xpt test1t test2t test3t Hellot Hit Bye
  def decompress(compressed: Array[Byte]): String = {
    Try {
      println("Starting decompression...")

      // Skip the first 7 bytes (remove metadata)
      val actualGzipData = compressed.drop(7)

      val bis = new ByteArrayInputStream(actualGzipData)
      val gis = new GZIPInputStream(bis)
      val bos = new ByteArrayOutputStream()

      val buffer = new Array[Byte](1024)
      var bytesRead: Int = 0

      while ( {
        bytesRead = gis.read(buffer); bytesRead != -1
      }) {
        bos.write(buffer, 0, bytesRead)
      }

      gis.close()
      bis.close()
      bos.close()

      val result = new String(bos.toByteArray, "UTF-8")
      println("Decompression complete.")
      result
    }.recover {
      case e: java.util.zip.ZipException =>
        println(s"ZIP Exception: ${e.getMessage}")
        ""
      case e: Exception =>
        println(s"General Exception: ${e.getMessage}")
        ""
    }.get
  }
}





// Code for Decompressed data: [B@4671e53b with Unknown object coding: 4 error
//package com.wingman
//
//import redis.clients.jedis.Jedis
//import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
//import java.util.zip.GZIPInputStream
//import scala.util.Try
//import akka.serialization.Serializer
//import boopickle.Default._
//import java.nio.ByteBuffer
//
//// Define the SnapshotMessage trait and SnapshotRecord case class
//sealed trait SnapshotMessage
//case class SnapshotRecord(sequenceNr: Long, timestamp: Long, snapshot: Array[Byte], ttl: Long) extends SnapshotMessage
//
//// Define the SnapshotRecordSerializer class for serialization and deserialization
//class SnapshotRecordSerializer extends Serializer {
//
//  // Unique ID for this serializer (required by Akka)
//  override def identifier: Int = 9002
//
//  // Akka does not need the manifest for this serializer
//  override def includeManifest: Boolean = false
//
//  // Convert from an object to a byte array
//  override def toBinary(obj: AnyRef): Array[Byte] = obj match {
//    case record: SnapshotRecord =>
//      println(s"Serializing SnapshotRecord: $record")
//      Pickle.intoBytes(record).array() // serialize the SnapshotRecord using BooPickle
//    case _ =>
//      throw new IllegalArgumentException(s"Cannot serialize object of type ${obj.getClass}")
//  }
//
//  // Convert from a byte array to an object
//  override def fromBinary(bytes: Array[Byte], manifest: Option[Class[_]]): AnyRef = {
//    println(s"Deserializing SnapshotRecord from bytes.")
//    Unpickle.apply[SnapshotRecord].fromBytes(ByteBuffer.wrap(bytes)) // deserialize using BooPickle
//  }
//}
//
//object RedisGzipDeserializer {
//  def main(args: Array[String]): Unit = {
//    val redisKey = "wingman-snapshot:user123"
//    val jedis = new Jedis("localhost", 6379) // Connect to Redis
//
//    println(s"Fetching key: $redisKey from Redis")
//    val compressedData: Array[Byte] = jedis.get(redisKey.getBytes())
//
//    if (compressedData == null) {
//      println("No data found for the given key.")
//    } else {
//      println(s"Retrieved ${compressedData.length} bytes from Redis")
//
//      val decompressedData = decompress(compressedData)
//      println(s"Decompressed data: $decompressedData")
//
//      // Deserialize into SnapshotRecord
//      val serializer = new SnapshotRecordSerializer()
//      val snapshotRecord = serializer.fromBinary(decompressedData, None).asInstanceOf[SnapshotRecord]
//
//      println(s"Deserialized SnapshotRecord: $snapshotRecord")
//    }
//
//    jedis.close()
//  }
//
//  // Decompressed data: [B@4671e53b with Unknown object coding: 4 error
//  def decompress(compressed: Array[Byte]): Array[Byte] = {
//    Try {
//      println("Starting decompression...")
//
//      // Skip the first 7 bytes (remove metadata)
//      val actualGzipData = compressed.drop(7)
//
//      val bis = new ByteArrayInputStream(actualGzipData)
//      val gis = new GZIPInputStream(bis)
//      val bos = new ByteArrayOutputStream()
//
//      val buffer = new Array[Byte](1024)
//      var bytesRead: Int = 0
//
//      while ( {
//        bytesRead = gis.read(buffer); bytesRead != -1
//      }) {
//        bos.write(buffer, 0, bytesRead)
//      }
//
//      gis.close()
//      bis.close()
//      bos.close()
//
//      val result = bos.toByteArray
//      println("Decompression complete.")
//      result
//    }.recover {
//      case e: java.util.zip.ZipException =>
//        println(s"ZIP Exception: ${e.getMessage}")
//        Array.emptyByteArray
//      case e: Exception =>
//        println(s"General Exception: ${e.getMessage}")
//        Array.emptyByteArray
//    }.get
//  }
//}
