export enum PriceInterval {
  PER_DAY = 'PER_DAY',
  PER_HOUR = 'PER_HOUR',
  PER_MONTH = 'PER_MONTH',
  PER_YEAR = 'PER_YEAR'
}

export const PriceIntervalLabels: Record<string, string> = {
  [PriceInterval.PER_DAY]:  'PER_DAY',
  [PriceInterval.PER_HOUR]:  'PER_HOUR',
  [PriceInterval.PER_MONTH]:  'PER_MONTH',
  [PriceInterval.PER_YEAR]:  'PER_YEAR',
}
