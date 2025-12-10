import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { CardModule } from 'primeng/card';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { ProjectModalComponent } from './project-modal/project-modal.component';
import { ProjectControllerApiService, ProjectDto } from '@anna/flow-board-api';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    TableModule,
    CardModule,
    TagModule,
    DialogModule,
    InputTextModule,
    DropdownModule,
    ProjectModalComponent
  ],
  templateUrl: './projects.component.html',
  styleUrl: './projects.component.scss'
})
export class ProjectsComponent implements OnInit {
  projects: ProjectDto[] = [];
  loading = false;
  showModal = false;
  selectedProject: ProjectDto | null = null;
  isEditMode = false;

  constructor(private projectService: ProjectControllerApiService) {}

  ngOnInit() {
    this.loadProjects();
  }

  loadProjects() {
    this.loading = true;
    this.projectService.getAllProjects().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading projects:', error);
        this.loading = false;
      }
    });
  }

  openCreateModal() {
    this.selectedProject = null;
    this.isEditMode = false;
    this.showModal = true;
  }

  openEditModal(project: ProjectDto) {
    this.selectedProject = { ...project };
    this.isEditMode = true;
    this.showModal = true;
  }

  onModalClose() {
    this.showModal = false;
    this.selectedProject = null;
    this.isEditMode = false;
  }

  onProjectSaved() {
    this.loadProjects();
    this.onModalClose();
  }

  deleteProject(project: ProjectDto) {
    if (project.id && confirm('Are you sure you want to delete this project?')) {
      this.projectService.deleteProject(project.id).subscribe({
        next: () => {
          this.loadProjects();
        },
        error: (error) => {
          console.error('Error deleting project:', error);
        }
      });
    }
  }

  getStatusSeverity(status: string | undefined) {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'COMPLETED':
        return 'info';
      case 'ARCHIVED':
        return 'warn';
      default:
        return 'secondary';
    }
  }

  getStatusLabel(status: string | undefined): string {
    switch (status) {
      case 'ACTIVE':
        return 'Active';
      case 'COMPLETED':
        return 'Completed';
      case 'ARCHIVED':
        return 'Archived';
      default:
        return 'Unknown';
    }
  }
}
