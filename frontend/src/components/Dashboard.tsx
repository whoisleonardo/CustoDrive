import type { Car } from '../lib/types'
import { compactMoney, money, number } from '../lib/format'

type Props = { cars: Car[]; onNew: () => void; onEdit: (car: Car) => void; loading: boolean }

export default function Dashboard({ cars, onNew, onEdit, loading }: Props) {
  const monthly = cars.map((car) => car.costs.monthlyCost)
  const initial = cars.map((car) => car.costs.initialDisbursement)
  const lowestMonthly = monthly.length ? Math.min(...monthly) : 0
  const lowestInitial = initial.length ? Math.min(...initial) : 0
  const averageMonthly = monthly.length ? monthly.reduce((a, b) => a + b, 0) / monthly.length : 0

  return <div className="page-content">
    <div className="page-intro"><div><span className="eyebrow">VISÃO GERAL · OUTUBRO 2026</span><h1>Decida com clareza.</h1><p>Compare o preço de entrar e o custo de viver com cada carro.</p></div><button className="button primary large" onClick={onNew}><span className="plus">+</span> Adicionar carro</button></div>
    <div className="metric-grid"><div className="metric-card dark"><span>Carros salvos</span><strong>{loading ? '—' : number(cars.length)}</strong><small>modelos na sua lista</small><i className="metric-mark">01</i></div><div className="metric-card"><span>Menor custo mensal</span><strong>{compactMoney(lowestMonthly)}</strong><small>entre os carros salvos</small><i className="metric-line blue" /></div><div className="metric-card"><span>Menor desembolso</span><strong>{compactMoney(lowestInitial)}</strong><small>compra + documentação</small><i className="metric-line amber" /></div><div className="metric-card"><span>Média mensal</span><strong>{compactMoney(averageMonthly)}</strong><small>referência da sua seleção</small><i className="metric-line green" /></div></div>
    <div className="section-heading"><div><span className="eyebrow">SUA SELEÇÃO</span><h2>Carros em consideração</h2></div><span className="count-badge">{cars.length} {cars.length === 1 ? 'carro' : 'carros'}</span></div>
    {loading ? <div className="loading-box">Carregando sua seleção…</div> : cars.length === 0 ? <div className="empty-box"><div className="empty-icon">+</div><h3>A sua lista começa aqui.</h3><p>Cadastre um carro e veja a planilha virar uma comparação viva.</p><button className="button primary" onClick={onNew}>Adicionar primeiro carro</button></div> : <div className="car-grid">{cars.map((car, index) => <article className="car-card" key={car.id} onClick={() => onEdit(car)}><div className="car-card-top"><div className="car-avatar">{String(index + 1).padStart(2, '0')}</div><div><span className="car-card-label">{car.brand || 'MODELO'} {car.modelYear || ''}</span><h3>{car.name}</h3><p>{[car.model, car.version].filter(Boolean).join(' · ') || 'Sem versão informada'}</p></div><button className="more-button" onClick={(event) => { event.stopPropagation(); onEdit(car) }}>•••</button></div><div className="car-card-body"><div><small>CUSTO MENSAL</small><strong>{money(car.costs.monthlyCost)}</strong><span>{money(car.costs.annualRecurringCost)} / ano</span></div><div><small>DESEMBOLSO INICIAL</small><strong>{money(car.costs.initialDisbursement)}</strong><span>compra à vista</span></div></div><div className="car-card-bottom"><span className="fuel-chip">{car.fuelType || 'Custo de uso'}</span>{car.specsSource && <span className="source-chip">{car.specsSource}</span>}<span className="open-link">Abrir ficha <b>↗</b></span></div></article>)}</div>}
  </div>
}
