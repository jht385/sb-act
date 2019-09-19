package com.example.sb.bean;

import java.io.Serializable;

import lombok.Data;

@Data
public class Person implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long pid;
	private String name;
	private Integer day;
}