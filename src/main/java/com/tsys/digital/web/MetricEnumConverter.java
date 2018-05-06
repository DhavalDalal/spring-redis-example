package com.tsys.digital.web;

import org.springframework.data.geo.Metrics;

import java.beans.PropertyEditorSupport;

public class MetricEnumConverter extends PropertyEditorSupport {
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    final String metric = text.toUpperCase();
    if (metric.startsWith("K")) {
      setValue(Metrics.KILOMETERS);
      return;
    }
    if (metric.startsWith("M")) {
      setValue(Metrics.MILES);
      return;
    }
    setValue(Metrics.NEUTRAL);
  }
}
