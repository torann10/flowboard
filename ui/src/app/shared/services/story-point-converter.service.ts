import { Injectable } from '@angular/core';

export interface CustomStoryPointMapping {
  storyPoints: number;
  timeValue: number; // in hours
  timeDisplay: string; // formatted time display
}

@Injectable({
  providedIn: 'root'
})
export class StoryPointConverterService {

  /**
   * Converts story points to time based on the project's story point time value
   * @param storyPoints Number of story points
   * @param storyPointTimeValue Duration in hours per story point
   * @returns Formatted time string (e.g., "2h 30m")
   */
  convertStoryPointsToTime(storyPoints: number, storyPointTimeValue: number): string {
    if (!storyPoints || !storyPointTimeValue || storyPoints <= 0 || storyPointTimeValue <= 0) {
      return '';
    }

    const totalHours = storyPoints * storyPointTimeValue;
    const hours = Math.floor(totalHours);
    const minutes = Math.round((totalHours - hours) * 60);

    if (hours > 0 && minutes > 0) {
      return `${hours}h ${minutes}m`;
    } else if (hours > 0) {
      return `${hours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }
    return '';
  }

  /**
   * Converts story points to ISO 8601 duration format
   * @param storyPoints Number of story points
   * @param storyPointTimeValue Duration in hours per story point
   * @returns ISO 8601 duration string (e.g., "PT2H30M")
   */
  convertStoryPointsToDuration(storyPoints: number, storyPointTimeValue: number): string {
    if (!storyPoints || !storyPointTimeValue || storyPoints <= 0 || storyPointTimeValue <= 0) {
      return '';
    }

    const totalHours = storyPoints * storyPointTimeValue;
    const hours = Math.floor(totalHours);
    const minutes = Math.round((totalHours - hours) * 60);

    if (hours > 0 || minutes > 0) {
      return `PT${hours}H${minutes}M`;
    }
    return '';
  }

  /**
   * Gets common story point values with their time equivalents
   * @param storyPointTimeValue Duration in hours per story point
   * @returns Array of objects with story points and time equivalents
   */
  getCommonStoryPointValues(storyPointTimeValue: number): Array<{storyPoints: number, time: string}> {
    const commonValues = [1, 2, 3, 5, 8, 13, 21];
    
    return commonValues.map(sp => ({
      storyPoints: sp,
      time: this.convertStoryPointsToTime(sp, storyPointTimeValue)
    }));
  }

  /**
   * Formats duration from hours to readable format
   * @param hours Number of hours
   * @returns Formatted time string
   */
  formatHoursToTime(hours: number): string {
    if (!hours || hours <= 0) {
      return '';
    }

    const wholeHours = Math.floor(hours);
    const minutes = Math.round((hours - wholeHours) * 60);

    if (wholeHours > 0 && minutes > 0) {
      return `${wholeHours}h ${minutes}m`;
    } else if (wholeHours > 0) {
      return `${wholeHours}h`;
    } else if (minutes > 0) {
      return `${minutes}m`;
    }
    return '';
  }

  /**
   * Converts story points to time using custom mappings
   * @param storyPoints Number of story points
   * @param customMappings Array of custom story point to time mappings
   * @param fallbackTimeValue Fallback time value per story point if no custom mapping exists
   * @returns Formatted time string
   */
  convertStoryPointsToTimeWithCustomMappings(
    storyPoints: number, 
    customMappings: CustomStoryPointMapping[], 
    fallbackTimeValue?: number
  ): string {
    if (!storyPoints || storyPoints <= 0) {
      return '';
    }

    // Look for exact match in custom mappings
    const customMapping = customMappings.find(m => m.storyPoints === storyPoints);
    if (customMapping) {
      return customMapping.timeDisplay;
    }

    // If no custom mapping found, use fallback linear calculation
    if (fallbackTimeValue && fallbackTimeValue > 0) {
      return this.convertStoryPointsToTime(storyPoints, fallbackTimeValue);
    }

    return '';
  }

  /**
   * Gets time value for story points using custom mappings
   * @param storyPoints Number of story points
   * @param customMappings Array of custom story point to time mappings
   * @param fallbackTimeValue Fallback time value per story point if no custom mapping exists
   * @returns Time value in hours
   */
  getTimeValueForStoryPoints(
    storyPoints: number, 
    customMappings: CustomStoryPointMapping[], 
    fallbackTimeValue?: number
  ): number {
    if (!storyPoints || storyPoints <= 0) {
      return 0;
    }

    // Look for exact match in custom mappings
    const customMapping = customMappings.find(m => m.storyPoints === storyPoints);
    if (customMapping) {
      return customMapping.timeValue;
    }

    // If no custom mapping found, use fallback linear calculation
    if (fallbackTimeValue && fallbackTimeValue > 0) {
      return storyPoints * fallbackTimeValue;
    }

    return 0;
  }

  /**
   * Gets all custom mappings with their time equivalents
   * @param customMappings Array of custom story point to time mappings
   * @returns Array of objects with story points and time equivalents
   */
  getCustomStoryPointValues(customMappings: CustomStoryPointMapping[]): Array<{storyPoints: number, time: string}> {
    return customMappings.map(mapping => ({
      storyPoints: mapping.storyPoints,
      time: mapping.timeDisplay
    }));
  }
}
