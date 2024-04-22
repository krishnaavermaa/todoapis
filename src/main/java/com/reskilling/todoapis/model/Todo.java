package com.reskilling.todoapis.model;



import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name="todo")
public class Todo {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long todoId;
	
	@ManyToOne
	@JoinColumn(name="userId",nullable=false)
	private User user;
	
	@NotBlank(message = "To-do title cannot be blank")
	private String title;
	
	private String description;
	
	@NotNull(message = "Invalid Date entered")
	@Future(message = "Date must be greater than current date")
	private LocalDate endDate;
	
	private boolean isCompleted;
	

}
