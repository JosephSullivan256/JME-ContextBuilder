package com.josephsullivan256.gmail.jme.contextbuilder;

import com.josephsullivan256.gmail.doxml.Element;

public interface Builder<T> {
	public T build(Element e);
}
