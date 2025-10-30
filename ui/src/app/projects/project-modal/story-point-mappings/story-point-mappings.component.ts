import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  ReactiveFormsModule,
  FormBuilder,
  FormGroup,
  FormArray,
  Validators,
  AbstractControl
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';

export interface StoryPointMapping {
  storyPoints: number;
  timeDisplay: string; // formatted time display
}

@Component({
  selector: 'app-story-point-mappings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    ButtonModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    CardModule,
    DividerModule
  ],
  templateUrl: './story-point-mappings.component.html',
  styleUrl: './story-point-mappings.component.scss'
})
export class StoryPointMappingsComponent implements OnInit {
  @Input() mappings: StoryPointMapping[] = [];
  @Input() projectId: string | undefined;
  @Output() mappingsChange = new EventEmitter<StoryPointMapping[]>();

  mappingsForm: FormGroup;
  errorMessage = '';

  constructor(private fb: FormBuilder) {
    this.mappingsForm = this.fb.group({
      mappings: this.fb.array([])
    });

    this.mappingsForm.valueChanges.subscribe(val => {
      this.emitMappings();
    })
  }

  ngOnInit() {
    this.initializeForm();
  }

  get mappingsArray(): FormArray {
    return this.mappingsForm.get('mappings') as FormArray;
  }

  initializeForm() {
    const mappingsArray = this.fb.array<FormGroup>([]);

    if (this.mappings.length > 0) {
      this.mappings.forEach(mapping => {
        mappingsArray.push(this.createMappingFormGroup(mapping));
      });
    } else {
      // Add default common story point values
      const defaultMappings = [
        { storyPoints: 1, timeDisplay: '2h' },
        { storyPoints: 2, timeDisplay: '4h' },
        { storyPoints: 3, timeDisplay: '6h' },
        { storyPoints: 5, timeDisplay: '10h' },
        { storyPoints: 8, timeDisplay: '16h' },
        { storyPoints: 13, timeDisplay: '26h' },
        { storyPoints: 21, timeDisplay: '42h' }
      ];

      defaultMappings.forEach(mapping => {
        mappingsArray.push(this.createMappingFormGroup(mapping));
      });
    }

    this.mappingsForm.setControl('mappings', mappingsArray);
  }

  createMappingFormGroup(mapping: StoryPointMapping = { storyPoints: 0, timeDisplay: '' }): FormGroup<any> {
    return this.fb.group({
      storyPoints: [mapping.storyPoints, [Validators.required, Validators.min(1), Validators.max(100)]],
      timeDisplay: [mapping.timeDisplay]
    });
  }

  addMapping() {
    const newMapping = this.createMappingFormGroup();
    this.mappingsArray.push(newMapping);
  }

  removeMapping(index: number) {
    this.mappingsArray.removeAt(index);
  }

  emitMappings() {
    console.log(this.mappingsArray);

    const mappings: StoryPointMapping[] = [];

    for (let i = 0; i < this.mappingsArray.length; i++) {
      const mappingGroup = this.mappingsArray.at(i);
      const storyPoints = mappingGroup.get('storyPoints')?.value;
      const timeDisplay = mappingGroup.get('timeDisplay')?.value;

      console.log(storyPoints, timeDisplay);

      if (storyPoints && timeDisplay && storyPoints > 0 && !!timeDisplay) {
        mappings.push({
          storyPoints,
          timeDisplay: timeDisplay
        });
      }
    }

    console.log(mappings);

    this.mappingsChange.emit(mappings);
  }

  getFieldError(formGroup: AbstractControl, fieldName: string): string {
    const field = formGroup.get(fieldName);
    if (field?.errors && field.touched) {
      if (field.errors['required']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} is required`;
      }
      if (field.errors['min']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at least ${field.errors['min'].min}`;
      }
      if (field.errors['max']) {
        return `${fieldName.charAt(0).toUpperCase() + fieldName.slice(1)} must be at most ${field.errors['max'].max}`;
      }
    }
    return '';
  }
}
