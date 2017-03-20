package com.dbortnichuk.lts.test

/**
  * Created by arcon on 20-Mar-17.
  */
trait TestUtil {

  def delta(target: Long = 1, percent: Int): Long = target * percent / 100

}
