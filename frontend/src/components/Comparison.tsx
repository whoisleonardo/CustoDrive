import type { Car } from '../lib/types'
import { money, number } from '../lib/format'

type Props = { cars: Car[]; onAdd: () => void }

export default function Comparison({ cars, onAdd }: Props) {
  const sorted = [...cars].sort((a, b) => a.costs.monthlyCost - b.costs.monthlyCost)
  const maxMonthly = Math.max(...cars.map((car) => car.costs.monthlyCost), 1)
  const bestMonthly = sorted[0]?.id
  const bestInitial = [...cars].sort((a, b) => a.costs.initialDisbursement - b.costs.initialDisbursement)[0]?.id
  return <div className="page-content">
    <div className="page-intro"><div><span className="eyebrow">LEITURA LADO A LADO</span><h1>Compare sem achismo.</h1><p>O menor preço de compra nem sempre é o menor custo para rodar.</p></div><button className="button primary large" onClick={onAdd}><span className="plus">+</span> Adicionar carro</button></div>
    {cars.length < 2 ? <div className="empty-box compact"><div className="empty-icon">02</div><h3>Adicione mais um carro para comparar.</h3><p>A tabela começa a revelar padrões a partir de dois modelos.</p><button className="button primary" onClick={onAdd}>Adicionar outro carro</button></div> : <>
      <div className="compare-highlight"><div><span className="eyebrow">MENOR MENSALIDADE</span><strong>{sorted[0].name}</strong><p>{money(sorted[0].costs.monthlyCost)} por mês · economia de {money(sorted[sorted.length - 1].costs.monthlyCost - sorted[0].costs.monthlyCost)} vs. maior custo</p></div><div className="highlight-number">{money(sorted[0].costs.annualRecurringCost)}<small>recorrente em 12 meses</small></div></div>
      <section className="comparison-panel"><div className="panel-head"><div><span className="eyebrow">CUSTO MENSAL ESTIMADO</span><h2>Onde o dinheiro vai</h2></div><span className="helper">IPVA + seguro + manutenção + energia + extras</span></div><div className="bar-list">{sorted.map((car) => <div className="bar-row" key={car.id}><div className="bar-label"><strong>{car.name}</strong><span>{money(car.costs.monthlyCost)}</span></div><div className="bar-track"><div className={`bar-fill ${car.id === bestMonthly ? 'best' : ''}`} style={{ width: `${Math.max((car.costs.monthlyCost / maxMonthly) * 100, 6)}%` }} /></div><small>{car.id === bestMonthly ? 'menor custo' : car.fuelType || 'uso mensal'}</small></div>)}</div></section>
      <section className="comparison-panel table-panel"><div className="panel-head"><div><span className="eyebrow">DETALHAMENTO</span><h2>Todos os números</h2></div><span className="legend"><i className="legend-dot blue" /> menor valor</span></div><div className="table-scroll"><table><thead><tr><th>Carro</th><th>Preço à vista</th><th>Inicial total</th><th>IPVA/mês</th><th>Energia/mês</th><th>Uso/mês</th><th>Uso em 12 meses</th></tr></thead><tbody>{sorted.map((car) => <tr key={car.id}><td><strong>{car.name}</strong><small>{[car.brand, car.model].filter(Boolean).join(' · ') || 'sem ficha técnica'}</small></td><td>{money(car.purchasePrice)}</td><td className={car.id === bestInitial ? 'best-text' : ''}>{money(car.costs.initialDisbursement)}</td><td>{money(car.costs.monthlyIpva)}</td><td>{money(car.costs.monthlyEnergy)}</td><td className={car.id === bestMonthly ? 'best-text' : ''}>{money(car.costs.monthlyCost)}</td><td>{money(car.costs.annualRecurringCost)}</td></tr>)}</tbody></table></div></section>
      <p className="footnote">Os valores são estimativas. A comparação não considera financiamento, revenda ou depreciação — exatamente como na planilha original.</p>
    </>}
  </div>
}
