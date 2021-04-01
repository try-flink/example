package org.example

import org.apache.flink.table.functions.ScalarFunction

class TestFunc extends ScalarFunction {
  def eval(s: String, begin: Integer, end: Integer): String = {
    s.substring(begin, end)
  }
}
