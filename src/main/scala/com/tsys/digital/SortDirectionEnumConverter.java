package com.tsys.digital;

import org.springframework.data.domain.Sort;

import java.beans.PropertyEditorSupport;

public class SortDirectionEnumConverter extends PropertyEditorSupport {
  @Override
  public void setAsText(String text) throws IllegalArgumentException {
    final String direction = text.toUpperCase();

    if (direction.startsWith("DES")) {
      setValue(Sort.Direction.DESC);
      return;
    }

    setValue(Sort.Direction.ASC);
  }
}
