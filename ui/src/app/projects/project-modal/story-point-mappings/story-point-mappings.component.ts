import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormsModule,
  AbstractControl,
  ControlValueAccessor,
  NG_VALUE_ACCESSOR
} from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputNumberModule } from 'primeng/inputnumber';
import { InputTextModule } from 'primeng/inputtext';
import { MessageModule } from 'primeng/message';
import { CardModule } from 'primeng/card';
import { DividerModule } from 'primeng/divider';
import { StoryPointTimeMappingDto } from '@anna/flow-board-api';
import { DurationFormatPipe } from '../../../shared/pipes/duration-format.pipe';
import { parseDurationInput } from '../../../shared/helpers/duration-helper';
import { Tooltip } from 'primeng/tooltip';

@Component({
  selector: 'app-story-point-mappings',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ButtonModule,
    InputNumberModule,
    InputTextModule,
    MessageModule,
    CardModule,
    DividerModule,
    DurationFormatPipe,
    Tooltip
  ],
  templateUrl: './story-point-mappings.component.html',
  styleUrl: './story-point-mappings.component.scss',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      multi: true,
      useExisting: StoryPointMappingsComponent
    }
  ]
})
export class StoryPointMappingsComponent implements ControlValueAccessor {
  mappings: StoryPointTimeMappingDto[] = [];
  onChange: any = (value: StoryPointTimeMappingDto[]) => {};
  onTouched: any = () => {};
  isDisabled = false;
  errorMessage = '';

  writeValue(obj: StoryPointTimeMappingDto[]): void {
    this.mappings = obj;
  }

  registerOnChange(fn: any): void {
    this.onChange = fn;
  }

  registerOnTouched(fn: any): void {
    this.onTouched = fn;
  }

  setDisabledState?(isDisabled: boolean): void {
    this.isDisabled = isDisabled;
  }

  addMapping() {
    this.mappings.push({
      storyPoints: 0,
      timeValue: '',
    } as StoryPointTimeMappingDto);
    this.onChange(this.mappings);
  }

  removeMapping(index: number) {
    this.mappings.splice(index, 1);
    this.onChange(this.mappings);
  }

  onStoryPointsChange(index: number, value: string) {
    const storyPoints = parseInt(value, 10);
    if (!isNaN(storyPoints)) {
      this.mappings[index].storyPoints = storyPoints;
      this.onChange(this.mappings);
    }
  }

  onTimeValueChange(index: number, value: Event) {
    const duration = parseDurationInput((value.target as any).value);
    if (duration) {
      this.mappings[index].timeValue = duration;
      this.onChange(this.mappings);
    }
  }
}
