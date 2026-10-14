import { useState } from 'react'
import type { CatalogFuelChoice, CatalogSearchResponse, CatalogVehicle } from '../lib/types'
import { decodeNhtsaVin, getFuelEconomyVehicle, searchFuelEconomy, searchLocalCatalog } from '../lib/api'
import { number } from '../lib/format'

type Props = { onUseVehicle: (vehicle: CatalogVehicle, choice?: CatalogFuelChoice) => void }

function compact(value?: number | null, suffix = '') {
  return value == null || !Number.isFinite(value) ? '—' : `${number(value)}${suffix}`
}

export default function Catalog({ onUseVehicle }: Props) {
  const [query, setQuery] = useState('')
  const [type, setType] = useState('all')
  const [year, setYear] = useState('')
  const [local, setLocal] = useState<CatalogSearchResponse | null>(null)
  const [page, setPage] = useState(1)
  const [epaMake, setEpaMake] = useState('')
  const [epaModel, setEpaModel] = useState('')
  const [epaYear, setEpaYear] = useState('2024')
  const [epaOptions, setEpaOptions] = useState<Array<{ id: string; label: string }>>([])
  const [epaVehicle, setEpaVehicle] = useState<CatalogVehicle | null>(null)
  const [vin, setVin] = useState('')
  const [vinYear, setVinYear] = useState('')
  const [vinVehicle, setVinVehicle] = useState<CatalogVehicle | null>(null)
  const [busy, setBusy] = useState('')
  const [message, setMessage] = useState('')
  const localSearch = async (requestedPage = 1) => {
    setBusy('local'); setMessage('Pesquisando os snapshots locais…')
    try { const result = await searchLocalCatalog(query.trim(), type, year, requestedPage); setLocal(result); setPage(requestedPage); setMessage(`${result.total} resultado(s) local(is). Nenhuma chamada externa foi feita.`) }
    catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível pesquisar o catálogo local.') }
    finally { setBusy('') }
  }
  const loadEpaOptions = async () => {
    setBusy('epa'); setEpaVehicle(null); setMessage('Consultando a base pública EPA dos Estados Unidos…')
    try { const result = await searchFuelEconomy(epaYear, epaMake.trim(), epaModel.trim()); setEpaOptions(result); setMessage(result.length ? `${result.length} versão(ões) EPA encontrada(s).` : 'A EPA não encontrou versões para essa combinação.') }
    catch (error) { setMessage(error instanceof Error ? error.message : 'A fonte EPA está indisponível.') }
    finally { setBusy('') }
  }
  const loadEpaVehicle = async (id: string, label: string) => {
    setBusy('epa'); setMessage('Carregando a ficha EPA selecionada…')
    try { setEpaVehicle(await getFuelEconomyVehicle(id, label)); setMessage('Ficha carregada. Confirme ano, versão e ciclo norte-americano antes de importar.') }
    catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível carregar a ficha EPA.') }
    finally { setBusy('') }
  }
  const decodeVin = async () => {
    setBusy('vin'); setVinVehicle(null); setMessage('Decodificando o VIN na NHTSA vPIC…')
    try { setVinVehicle(await decodeNhtsaVin(vin.trim(), vinYear)); setMessage('VIN decodificado. A NHTSA pode não retornar dimensões ou consumo para todos os veículos.') }
    catch (error) { setMessage(error instanceof Error ? error.message : 'Não foi possível decodificar esse VIN.') }
    finally { setBusy('') }
  }

  const vehicleCard = (vehicle: CatalogVehicle) => <article className="free-vehicle-card" key={vehicle.id}>
    <div className="free-vehicle-head"><div><span className="source-chip">{vehicle.source}</span><h3>{[vehicle.make, vehicle.model].filter(Boolean).join(' ') || 'Veículo identificado'} {vehicle.trim && <small>· {vehicle.trim}</small>}</h3><p>{[vehicle.modelYear, vehicle.category, vehicle.fuelType, vehicle.powertrain].filter(Boolean).join(' · ')}</p></div></div>
    <div className="free-spec-grid">
      {(vehicle.consumptionPer100 != null || vehicle.fuelChoices.length > 0) && <div><small>Consumo de referência</small><strong>{vehicle.fuelChoices.length > 0 ? `${vehicle.fuelChoices.length} opção(ões)` : `${compact(vehicle.consumptionPer100)} ${vehicle.consumptionUnit || ''}`}</strong></div>}
      {vehicle.batteryKwh != null && <div><small>Bateria</small><strong>{compact(vehicle.batteryKwh, ' kWh')}</strong></div>}
      {vehicle.rangeKm != null && <div><small>Autonomia {vehicle.rangeCycle ? `(${vehicle.rangeCycle})` : ''}</small><strong>{compact(vehicle.rangeKm, ' km')}</strong></div>}
      {vehicle.lengthMm != null && <div><small>Comprimento</small><strong>{compact(vehicle.lengthMm, ' mm')}</strong></div>}
      {vehicle.widthMm != null && <div><small>Largura</small><strong>{compact(vehicle.widthMm, ' mm')}</strong></div>}
      {vehicle.heightMm != null && <div><small>Altura</small><strong>{compact(vehicle.heightMm, ' mm')}</strong></div>}
      {vehicle.wheelbaseMm != null && <div><small>Entre-eixos</small><strong>{compact(vehicle.wheelbaseMm, ' mm')}</strong></div>}
      {vehicle.curbWeightKg != null && <div><small>Peso</small><strong>{compact(vehicle.curbWeightKg, ' kg')}</strong></div>}
      {vehicle.doors != null && <div><small>Portas</small><strong>{vehicle.doors}</strong></div>}
      {vehicle.seats != null && <div><small>Lugares</small><strong>{vehicle.seats}</strong></div>}
      {vehicle.transmission && <div><small>Câmbio</small><strong>{vehicle.transmission}</strong></div>}
      {vehicle.driveType && <div><small>Tração</small><strong>{vehicle.driveType}</strong></div>}
    </div>
    {vehicle.fuelChoices.length > 0 && <div className="fuel-choice-list"><small>Importe uma opção de ciclo/combustível por vez:</small>{vehicle.fuelChoices.map((choice, index) => <button key={`${vehicle.id}-${choice.fuelType}-${index}`} className="button secondary" onClick={() => onUseVehicle(vehicle, choice)}>{choice.label}: {compact(choice.consumptionPer100)} {choice.unit} · usar</button>)}</div>}
    <div className="free-card-footer"><div className="source-links">{vehicle.sources.map((source, index) => <a key={`${vehicle.id}-${index}`} href={source.url || '#'} target="_blank" rel="noreferrer">Fonte: {source.name}{source.version ? ` · ${source.version}` : ''}</a>)}<span>Campos não publicados pela fonte permanecem vazios.</span></div>{vehicle.fuelChoices.length === 0 && <button className="button primary" onClick={() => onUseVehicle(vehicle)}>Importar ficha</button>}</div>
  </article>

  return <div className="page-content catalog-page free-catalog">
    <div className="page-intro"><div><span className="eyebrow">DADOS AUTOMOTIVOS GRATUITOS</span><h1>Pesquise em fontes abertas.</h1><p>Começamos por dados locais do Inmetro e Open EV Data. EPA e VIN são consultas externas opcionais, nunca automáticas.</p></div><span className="api-badge"><span className="api-dot" /> Sem chave paga</span></div>

    <div className="free-source-notice"><strong>Escopo e precisão</strong><span>O snapshot PBEV 2026 desta versão estruturou 855 registros; a tabela oficial informa 976 modelos/versões, portanto a cobertura local ainda é parcial. Confira versão e consumo antes de salvar. O Open EV Data está filtrado para mercado BR; campos não disponíveis não são estimados.</span></div>

    <section className="catalog-card free-search-card"><div className="catalog-card-head"><div className="catalog-icon">01</div><div><h2>Pesquisar catálogo local</h2><p>Inmetro PBEV (consumo brasileiro) + Open EV Data (fichas elétricas). Funciona sem rede após o Compose iniciar.</p></div></div>
      <form className="free-search-form" onSubmit={(event) => { event.preventDefault(); void localSearch(1) }}>
        <label>Marca ou modelo<input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Ex.: BYD King, Geely EX2, Corolla" minLength={2} required /></label>
        <label>Propulsão<select value={type} onChange={(event) => setType(event.target.value)}><option value="all">Todas</option><option value="combustion">Combustão / flex</option><option value="hybrid">Híbrido (inclui plug-in)</option><option value="phev">Somente plug-in</option><option value="electric">Elétrico (100%)</option></select></label>
        <label>Ano do modelo<input type="number" min="1984" max="2100" value={year} onChange={(event) => setYear(event.target.value)} placeholder="Qualquer ano" /></label>
        <button className="button primary" disabled={busy === 'local'}>{busy === 'local' ? 'Pesquisando…' : 'Pesquisar localmente'}</button>
      </form>
      {local && <><div className="free-results-meta">{local.total} registro(s) · página {local.page}{local.total > 0 ? ` de ${Math.ceil(local.total / local.pageSize)}` : ''}</div><div className="free-results-list">{local.results.map(vehicleCard)}{local.results.length === 0 && <p className="catalog-empty">Nenhum resultado com estes filtros. Remova o ano ou a propulsão e tente novamente. BYD King aparece como híbrido plug-in (PBEV 2026); Geely EX2 consta no PBEV 2026 e no Open EV Data 2025. A EPA dos EUA também está disponível abaixo.</p>}</div>{local.total > local.pageSize && <div className="catalog-pagination"><button className="button tiny" disabled={busy !== '' || page <= 1} onClick={() => void localSearch(page - 1)}>Anterior</button><span>{page} / {Math.ceil(local.total / local.pageSize)}</span><button className="button tiny" disabled={busy !== '' || page >= Math.ceil(local.total / local.pageSize)} onClick={() => void localSearch(page + 1)}>Próxima</button></div>}</>}
    </section>

    <section className="catalog-card"><div className="catalog-card-head"><div className="catalog-icon">02</div><div><h2>Consulta opcional EPA (EUA)</h2><p>Fallback para combustão, híbridos e elétricos quando o modelo não aparecer no catálogo local.</p></div></div>
      <div className="external-source-warning"><strong>Dados norte-americanos.</strong> Consumos e classificações usam o ciclo EPA e podem não representar uma versão vendida no Brasil. A consulta acontece só ao clicar; não usa chave nem gera cobrança do fornecedor.</div>
      <form className="free-search-form" onSubmit={(event) => { event.preventDefault(); void loadEpaOptions() }}><label>Marca<input value={epaMake} onChange={(event) => setEpaMake(event.target.value)} required placeholder="Toyota" /></label><label>Modelo<input value={epaModel} onChange={(event) => setEpaModel(event.target.value)} required placeholder="Corolla" /></label><label>Ano<input type="number" min="1984" max="2100" value={epaYear} onChange={(event) => setEpaYear(event.target.value)} required /></label><button className="button secondary" disabled={busy === 'epa'}>{busy === 'epa' ? 'Consultando…' : 'Buscar versões EPA'}</button></form>
      {epaOptions.length > 0 && <div className="epa-option-list">{epaOptions.map((option) => <button key={option.id} className="catalog-result select-result" disabled={busy === 'epa'} onClick={() => void loadEpaVehicle(option.id, option.label)}><strong>{option.label}</strong><span>{epaVehicle?.id === `fueleconomy-${option.id}` ? 'Carregada' : 'Ver ficha'}</span></button>)}</div>}
      {epaVehicle && vehicleCard(epaVehicle)}
    </section>

    <section className="catalog-card"><div className="catalog-card-head"><div className="catalog-icon">03</div><div><h2>Decodificar VIN (NHTSA)</h2><p>Busca sob demanda de fabricante, modelo, ano e campos que a base consiga identificar.</p></div></div>
      <div className="external-source-warning">A NHTSA é uma fonte dos Estados Unidos. Decodificar VIN não garante ficha completa; dimensões, consumo e outros campos podem vir em branco.</div>
      <form className="free-search-form vin-search-form" onSubmit={(event) => { event.preventDefault(); void decodeVin() }}><label>VIN (17 caracteres)<input value={vin} onChange={(event) => setVin(event.target.value.toUpperCase())} minLength={17} maxLength={17} required placeholder="17 caracteres" /></label><label>Ano do modelo (opcional)<input type="number" min="1980" max="2100" value={vinYear} onChange={(event) => setVinYear(event.target.value)} placeholder="Ex.: 2022" /></label><button className="button secondary" disabled={busy === 'vin'}>{busy === 'vin' ? 'Decodificando…' : 'Decodificar VIN'}</button></form>
      {vinVehicle && vehicleCard(vinVehicle)}
    </section>
    {message && <div className="catalog-message">{message}</div>}
  </div>
}
