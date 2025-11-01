export const parseDurationInput = (input: string): string => {
  if (!input) return '';

  // Convert from user-friendly format (2h 30m) to ISO 8601 format (PT2H30M)
  const hourMatch = input.match(/(\d+)h/);
  const minuteMatch = input.match(/(\d+)m/);

  const hours = hourMatch ? parseInt(hourMatch[1]) : 0;
  const minutes = minuteMatch ? parseInt(minuteMatch[1]) : 0;

  if (hours > 0 || minutes > 0) {
    return `PT${hours}H${minutes}M`;
  }

  return '';
}
