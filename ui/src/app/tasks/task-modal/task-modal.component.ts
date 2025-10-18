import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { TextareaModule } from 'primeng/textarea';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { SelectModule } from 'primeng/select';
import { InputMaskModule } from 'primeng/inputmask';

@Component({
  selector: 'app-create-task',
  imports: [DialogModule, ButtonModule, InputTextModule, TextareaModule, ReactiveFormsModule, CommonModule, SelectModule, InputMaskModule],
  templateUrl: './task-modal.component.html',
  styleUrl: './task-modal.component.scss'
})
export class TaskModalComponent implements OnInit {
  @Input() visible: boolean = false;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() taskCreated = new EventEmitter<any>();

  createTaskForm!: FormGroup;
  statuses = [
    {label: 'To Do', value: 'to-do'},
    {label: 'In Progress', value: 'in-progress'},
    {label: 'Done', value: 'done'}
  ];
  projects = [
    { label: 'Project A', value: 'project-a' },
    { label: 'Project B', value: 'project-b' },
    { label: 'Project C', value: 'project-c' }
  ];

  constructor(private fb: FormBuilder) {}

  ngOnInit() {
    this.createTaskForm = this.fb.group({
      title: ['', Validators.required],
      assignee: [''],
      project: ['', Validators.required],
      estimatedTime: [null],
      status: [null],
      description: ['']
    });
  }

  createTask() {
    if (this.createTaskForm.valid) {
      this.taskCreated.emit(this.createTaskForm.value);
      this.close();
    } else {
      this.createTaskForm.markAllAsTouched();
    }
  }

  close() {
    this.visible = false;
    this.visibleChange.emit(false);
    this.createTaskForm.reset();
  }

  setVisibility(visible: boolean) {
    this.visible = visible;
    this.visibleChange.emit(visible);
  }

}
