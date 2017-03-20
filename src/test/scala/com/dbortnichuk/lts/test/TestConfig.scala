package com.dbortnichuk.lts.test

import java.io.File

import com.typesafe.config.{Config, ConfigFactory}

trait TestConfig {

  def getConfig(fileName: String): Config = {
    ConfigFactory.parseFile(new File(this.getClass.getClassLoader.getResource(fileName).getFile)).resolve()
  }

}
