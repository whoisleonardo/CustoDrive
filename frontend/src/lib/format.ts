export const money = (value: number) => new Intl.NumberFormat('pt-BR', {
  style: 'currency', currency: 'BRL', minimumFractionDigits: 2, maximumFractionDigits: 2,
}).format(value || 0)

export const number = (value: number | null | undefined, digits = 0) => value == null
  ? 'Não informado'
  : new Intl.NumberFormat('pt-BR', { maximumFractionDigits: digits }).format(value)

export const compactMoney = (value: number) => new Intl.NumberFormat('pt-BR', {
  style: 'currency', currency: 'BRL', notation: 'compact', maximumFractionDigits: 1,
}).format(value || 0)
