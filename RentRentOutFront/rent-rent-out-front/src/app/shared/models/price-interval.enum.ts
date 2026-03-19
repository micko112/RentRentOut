export enum PriceInterval {
  PER_DAY = 'PER_DAY',
  PER_HOUR = 'PER_HOUR',
  PER_MONTH = 'PER_MONTH',
  PER_YEAR = 'PER_YEAR'
}

export const PriceIntervalLabels: Record<string, string> = {
  [PriceInterval.PER_DAY]:   'Po danu',
  [PriceInterval.PER_HOUR]:  'Po satu',
  [PriceInterval.PER_MONTH]: 'Po mesecu',
  [PriceInterval.PER_YEAR]:  'Po godini',
}
