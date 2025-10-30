import { Component, Input, Output, EventEmitter, OnInit, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DropdownModule } from 'primeng/dropdown';
import { TableModule } from 'primeng/table';
import { TagModule } from 'primeng/tag';
import { DialogModule } from 'primeng/dialog';
import { MessageModule } from 'primeng/message';
import {
  UserResponse,
  ProjectUserDto,
  ProjectUserCreateRequestDto,
  ProjectUserUpdateRequestDto
} from '@anna/flow-board-api';
import { UserControllerApiService, ProjectUserControllerApiService } from '@anna/flow-board-api';

@Component({
  selector: 'app-user-assignment',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputTextModule,
    DropdownModule,
    TableModule,
    TagModule,
    DialogModule,
    MessageModule
  ],
  templateUrl: './user-assignment.component.html',
  styleUrl: './user-assignment.component.scss'
})
export class UserAssignmentComponent implements OnInit, OnChanges {
  @Input() projectId: string | undefined;
  @Input() visible = false;
  @Output() close = new EventEmitter<void>();
  @Output() usersUpdated = new EventEmitter<void>();

  allUsers: UserResponse[] = [];
  projectUsers: ProjectUserDto[] = [];
  availableUsers: UserResponse[] = [];
  selectedUser: UserResponse | null = null;
  selectedRole: string = 'MEMBER';
  loading = false;
  errorMessage = '';

  roleOptions = [
    { label: 'Maintainer', value: 'MAINTAINER' },
    { label: 'Member', value: 'MEMBER' },
    { label: 'Viewer', value: 'VIEWER' }
  ];

  constructor(
    private userService: UserControllerApiService,
    private projectUserService: ProjectUserControllerApiService
  ) {}

  ngOnInit() {
    if (this.projectId) {
      this.loadData();
    }
  }

  ngOnChanges() {
    if (this.projectId && this.visible) {
      this.loadData();
    }
  }

  loadData() {
    this.loading = true;
    this.errorMessage = '';

    // Load all users
    this.userService.getAllUsers().subscribe({
      next: (users) => {
        this.allUsers = users;
        this.loadProjectUsers();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error loading users';
        console.error('Error loading users:', error);
      }
    });
  }

  loadProjectUsers() {
    this.projectUserService.getAllProjectUsers().subscribe({
      next: (projectUsers) => {
        this.projectUsers = projectUsers.filter(pu => pu.projectId === this.projectId);
        this.updateAvailableUsers();
        this.loading = false;
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error loading project users';
        console.error('Error loading project users:', error);
      }
    });
  }

  updateAvailableUsers() {
    const assignedUserIds = this.projectUsers.map(pu => pu.userId);
    this.availableUsers = this.allUsers.filter(user =>
      !assignedUserIds.includes(user.id)
    );
  }

  assignUser() {
    if (!this.selectedUser || !this.projectId) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const projectUser: ProjectUserCreateRequestDto = {
      userId: this.selectedUser.id!,
      projectId: this.projectId,
      role: this.selectedRole as any
    };

    this.projectUserService.createProjectUser(projectUser).subscribe({
      next: () => {
        this.loadProjectUsers();
        this.selectedUser = null;
        this.selectedRole = 'MEMBER';
        this.usersUpdated.emit();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error assigning user to project';
        console.error('Error assigning user:', error);
      }
    });
  }

  removeUser(projectUser: ProjectUserDto) {
    if (!projectUser.id) {
      return;
    }

    if (confirm('Are you sure you want to remove this user from the project?')) {
      this.loading = true;
      this.errorMessage = '';

      this.projectUserService.deleteProjectUser(projectUser.id).subscribe({
        next: () => {
          this.loadProjectUsers();
          this.usersUpdated.emit();
        },
        error: (error) => {
          this.loading = false;
          this.errorMessage = 'Error removing user from project';
          console.error('Error removing user:', error);
        }
      });
    }
  }

  updateUserRole(projectUser: ProjectUserDto, newRole: string) {
    if (!projectUser.id) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const updatedProjectUser: ProjectUserUpdateRequestDto = {
      ...projectUser,
      role: newRole as any
    };

    this.projectUserService.updateProjectUser(projectUser.id, updatedProjectUser).subscribe({
      next: () => {
        this.loadProjectUsers();
        this.usersUpdated.emit();
      },
      error: (error) => {
        this.loading = false;
        this.errorMessage = 'Error updating user role';
        console.error('Error updating user role:', error);
      }
    });
  }

  getUserName(userId: string): string {
    const user = this.allUsers.find(u => u.id === userId);
    return user ? `${user.firstName} ${user.lastName}` : 'Unknown User';
  }

  getUserEmail(userId: string): string {
    const user = this.allUsers.find(u => u.id === userId);
    return user ? user.emailAddress! : '';
  }

  getRoleSeverity(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'danger';
      case 'MEMBER':
        return 'success';
      case 'VIEWER':
        return 'info';
      default:
        return 'secondary';
    }
  }

  getRoleLabel(role: string): string {
    switch (role) {
      case 'ADMIN':
        return 'Admin';
      case 'MEMBER':
        return 'Member';
      case 'VIEWER':
        return 'Viewer';
      default:
        return 'Unknown';
    }
  }

  onClose() {
    this.close.emit();
  }
}
