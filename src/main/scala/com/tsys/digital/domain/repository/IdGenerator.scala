package com.tsys.digital.domain.repository

//import java.util.UUID
import java.util.concurrent.atomic.AtomicLong

import org.springframework.stereotype.Component

@Component
class IdGenerator {
  private val id : AtomicLong = new AtomicLong(0)

  def generateId = id.incrementAndGet.toString
//  def generateId = UUID.randomUUID().toString
}
