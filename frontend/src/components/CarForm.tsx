import { useState } from 'react'
import type { FormEvent } from 'react'
import type { Car, CarInput, CatalogFuelChoice, CatalogVehicle, FormState } from '../lib/types'
import { searchLocalCatalog } from '../lib/api'
import { number } from '../lib/format'

type Props = {
  car?: Car | null
  initialValues?: Partial<CarInput>
  onCancel: () => void
  onSubmit: (payload: CarInput) => Promise<void>
  onDelete?: () => Promise<void>
}

const defaults: FormState = {
  name: '', brand: '', model: '', version: '', modelYear: '', vin: '',
  purchasePrice: '', initialCosts: '2500', ipvaRatePercent: '3.5', annualInsurance: '4500', annualMaintenance: '1200',
  consumptionPer100: '15', energyUnitPrice: '0.95', monthlyKm: '720', monthlyParking: '130', monthlyOtherCosts: '0',
  bodyClass: '', fuelType: '', driveType: '', transmission: '', doors: '', seats: '', lengthMm: '', widthMm: '', heightMm: '',
  wheelbaseMm: '', curbWeightKg: '', batteryKwh: '', electrificationLevel: '', specsSource: '',
}

const fromCar = (car?: Car | null, initialValues?: Partial<CarInput>): FormState => {
  if (car) {
    return { ...defaults, ...Object.fromEntries(Object.keys(defaults).map((key) => {
      const value = car[key as keyof Car]
      return [key, value == null ? '' : String(value)]
    })) } as FormState
  }
  const prefill = Object.fromEntries(Object.keys(defaults).filter((key) => initialValues?.[key as keyof CarInput] != null).map((key) => [key, String(initialValues?.[key as keyof CarInput])]))
  const result = { ...defaults, ...prefill } as FormState
  if (initialValues && Object.keys(initialValues).length > 0) {
    if (initialValues.consumptionPer100 == null) result.consumptionPer100 = ''
    if (initialValues.energyUnitPrice == null) result.energyUnitPrice = ''
  }
  return result
}

const numericKeys: Array<keyof FormState> = [
  'modelYear', 'purchasePrice', 'initialCosts', 'ipvaRatePercent', 'annualInsurance', 'annualMaintenance',
  'consumptionPer100', 'energyUnitPrice', 'monthlyKm', 'monthlyParking', 'monthlyOtherCosts', 'doors', 'seats',
  'lengthMm', 'widthMm', 'heightMm', 'wheelbaseMm', 'curbWeightKg', 'batteryKwh',
]

const toPayload = (form: FormState): CarInput => {
  const payload: Record<string, unknown> = { ...form }
  numericKeys.forEach((key) => { payload[key] = form[key] === '' ? null : Number(form[key]) })
  return payload as CarInput
}

function Field({ label, name, value, onChange, type = 'text', placeholder, step = 'any', suffix, required = false }: {
  label: string; name: keyof FormState; value: string; onChange: (name: keyof FormState, value: string) => void; type?: string; placeholder?: string; step?: string; suffix?: string; required?: boolean
}) {
  return <label className="field">
    <span>{label}{required && <b className="required"> *</b>}</span>
    <div className="input-wrap">
      <input type={type} inputMode={type === 'number' ? 'decimal' : undefined} name={name} value={value} onChange={(event) => onChange(name, event.target.value)} placeholder={placeholder} step={step} min={type === 'number' ? 0 : undefined} required={required} />
      {suffix && <small>{suffix}</small>}
    </div>
  </label>
}

function showValue(value?: number | null, suffix = '') {
  return value == null ? '—' : `${number(value)}${suffix}`
}

export default function CarForm({ car, initialValues, onCancel, onSubmit, onDelete }: Props) {
  const [form, setForm] = useState<FormState>(() => fromCar(car, initialValues))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [lookupBusy, setLookupBusy] = useState(false)
  const [catalogMessage, setCatalogMessage] = useState('')
  const [catalogMatches, setCatalogMatches] = useState<CatalogVehicle[]>([])
  const isElectric = /eletric|electric|kwh/i.test(form.fuelType)

  const update = (name: keyof FormState, value: string) => {
    setForm((previous) => ({ ...previous, [name]: value }))
    if (['name', 'brand', 'model', 'version'].includes(name)) { setCatalogMatches([]); setCatalogMessage('') }
  }

  const findLocalData = async () => {
    const identity = [form.brand, form.model].map((part) => part.trim()).filter(Boolean).join(' ') || form.name.trim()
    if (identity.length < 2) { setCatalogMessage('Informe pelo menos a marca ou o modelo antes de pesquisar.'); return }
    setLookupBusy(true); setCatalogMessage('Pesquisando os snapshots locais gratuitos…'); setCatalogMatches([])
    try {
      const result = await searchLocalCatalog(identity, 'all', '', 1)
      setCatalogMatches(result.results)
      setCatalogMessage(result.total > 0
        ? `${result.total} correspondência(s). Confira ano e versão: fichas diferentes não são misturadas automaticamente.`
        : 'Nenhum resultado local para essa grafia. Você pode tentar uma consulta EPA na aba Catálogo gratuito.')
    } catch (lookupError) {
      setCatalogMessage(lookupError instanceof Error ? lookupError.message : 'Não foi possível pesquisar os dados locais.')
    } finally { setLookupBusy(false) }
  }

  const applyLocalData = (vehicle: CatalogVehicle, choice?: CatalogFuelChoice) => {
    setForm((previous) => {
      const keepOr = (value: string | number | null | undefined, current: string) => value == null || value === '' ? current : String(value)
      const sources = Array.from(new Set([previous.specsSource, vehicle.source].filter((source): source is string => Boolean(source))))
      return {
        ...previous,
        fuelType: choice?.fuelType || previous.fuelType,
        ...(choice?.consumptionPer100 != null ? { consumptionPer100: String(choice.consumptionPer100) } : {}),
        bodyClass: keepOr(vehicle.category, previous.bodyClass),
        driveType: keepOr(vehicle.driveType, previous.driveType),
        transmission: keepOr(vehicle.transmission, previous.transmission),
        doors: keepOr(vehicle.doors, previous.doors),
        seats: keepOr(vehicle.seats, previous.seats),
        lengthMm: keepOr(vehicle.lengthMm, previous.lengthMm),
        widthMm: keepOr(vehicle.widthMm, previous.widthMm),
        heightMm: keepOr(vehicle.heightMm, previous.heightMm),
        wheelbaseMm: keepOr(vehicle.wheelbaseMm, previous.wheelbaseMm),
        curbWeightKg: keepOr(vehicle.curbWeightKg, previous.curbWeightKg),
        batteryKwh: keepOr(vehicle.batteryKwh, previous.batteryKwh),
        electrificationLevel: keepOr(vehicle.powertrain, previous.electrificationLevel),
        specsSource: sources.join(' + '),
      }
    })
    setCatalogMessage(choice
      ? `Dados técnicos e consumo de ${choice.label} aplicados. Preço de compra, demais custos e preço local por unidade foram preservados; confira-os antes de salvar.`
      : 'Ficha técnica aplicada. Combustível, consumo e preço por unidade permaneceram intactos para não mudar a unidade do cálculo; escolha uma opção de consumo abaixo ou preencha-os manualmente.')
  }

  const submit = async (event: FormEvent) => {
    event.preventDefault(); setError(''); setSaving(true)
    try { await onSubmit(toPayload(form)) } catch (err) { setError(err instanceof Error ? err.message : 'Não foi possível salvar o carro.') } finally { setSaving(false) }
  }

  return <form className="drawer-form" onSubmit={submit}>
    <div className="drawer-head">
      <div><span className="eyebrow">{car ? 'Edição' : 'Novo registro'}</span><h2>{car ? 'Ajustar carro' : 'Adicionar carro'}</h2><p>Custos à vista e recorrentes para uma comparação justa.</p></div>
      <button type="button" className="icon-button" onClick={onCancel} aria-label="Fechar">×</button>
    </div>
    {error && <div className="alert error">{error}</div>}

    <section className="form-section"><div className="section-title"><span className="section-number">01</span><div><h3>Identificação</h3><p>Como você quer reconhecer este carro?</p></div></div>
      <div className="form-grid two"><Field label="Nome da comparação" name="name" value={form.name} onChange={update} placeholder="Ex.: Geely EX2 Max" required /><Field label="Marca" name="brand" value={form.brand} onChange={update} placeholder="Geely, BYD…" /><Field label="Modelo" name="model" value={form.model} onChange={update} placeholder="EX2" /><Field label="Versão" name="version" value={form.version} onChange={update} placeholder="Max" /><Field label="Ano do modelo" name="modelYear" value={form.modelYear} onChange={update} type="number" placeholder="2025" /></div>
    </section>

    <section className="form-section"><div className="section-title"><span className="section-number">02</span><div><h3>Custos do simulador</h3><p>Consumo em {isElectric ? 'kWh/100 km' : 'unidades/100 km'}; informe o preço local real da energia ou do combustível.</p></div></div>
      <div className="form-grid two">
        <Field label="Preço à vista" name="purchasePrice" value={form.purchasePrice} onChange={update} type="number" suffix="R$" required />
        <Field label="Documentação e custos iniciais" name="initialCosts" value={form.initialCosts} onChange={update} type="number" suffix="R$" required />
        <Field label="IPVA anual" name="ipvaRatePercent" value={form.ipvaRatePercent} onChange={update} type="number" suffix="%" required />
        <Field label="Seguro anual" name="annualInsurance" value={form.annualInsurance} onChange={update} type="number" suffix="R$" required />
        <Field label="Manutenção anual" name="annualMaintenance" value={form.annualMaintenance} onChange={update} type="number" suffix="R$" required />
        <Field label={isElectric ? 'Consumo elétrico por 100 km' : 'Consumo por 100 km'} name="consumptionPer100" value={form.consumptionPer100} onChange={update} type="number" suffix={isElectric ? 'kWh' : 'L ou equivalente'} required />
        <Field label={isElectric ? 'Preço local por kWh' : 'Preço local por unidade de combustível'} name="energyUnitPrice" value={form.energyUnitPrice} onChange={update} type="number" suffix="R$" required />
        <Field label="Quilometragem mensal" name="monthlyKm" value={form.monthlyKm} onChange={update} type="number" suffix="km" required />
        <Field label="Estacionamento mensal" name="monthlyParking" value={form.monthlyParking} onChange={update} type="number" suffix="R$" required />
        <Field label="Outros custos mensais" name="monthlyOtherCosts" value={form.monthlyOtherCosts} onChange={update} type="number" suffix="R$" required />
      </div>
    </section>

    <section className="form-section"><div className="section-title"><span className="section-number">03</span><div><h3>Ficha técnica</h3><p>Os dados não são buscados automaticamente ao abrir ou salvar este formulário.</p></div></div>
      <div className="local-enrichment-tools">
        <button type="button" className="button secondary" onClick={() => void findLocalData()} disabled={lookupBusy}>{lookupBusy ? 'Pesquisando…' : 'Buscar dados gratuitos para este carro'}</button>
        <p>Pesquisa somente os snapshots locais do Inmetro/Open EV Data; não sobrescreve seus custos nem consulta serviços externos.</p>
        {catalogMessage && <div className="catalog-message inline-message">{catalogMessage}</div>}
        {catalogMatches.length > 0 && <div className="enrichment-results">{catalogMatches.map((vehicle) => <article className="enrichment-match" key={vehicle.id}>
          <div className="enrichment-match-title"><div><strong>{[vehicle.make, vehicle.model].filter(Boolean).join(' ')}{vehicle.trim ? ` · ${vehicle.trim}` : ''}</strong><span>{[vehicle.modelYear, vehicle.powertrain, vehicle.fuelType, vehicle.source].filter(Boolean).join(' · ')}</span></div><button type="button" className="button tiny secondary" onClick={() => applyLocalData(vehicle)}>Aplicar ficha técnica</button></div>
          <div className="enrichment-specs">{vehicle.batteryKwh != null && <span>Bateria {showValue(vehicle.batteryKwh, ' kWh')}</span>}{vehicle.rangeKm != null && <span>Autonomia {showValue(vehicle.rangeKm, ' km')}</span>}{vehicle.lengthMm != null && <span>Comprimento {showValue(vehicle.lengthMm, ' mm')}</span>}{vehicle.widthMm != null && <span>Largura {showValue(vehicle.widthMm, ' mm')}</span>}{vehicle.heightMm != null && <span>Altura {showValue(vehicle.heightMm, ' mm')}</span>}{vehicle.wheelbaseMm != null && <span>Entre-eixos {showValue(vehicle.wheelbaseMm, ' mm')}</span>}{vehicle.curbWeightKg != null && <span>Peso {showValue(vehicle.curbWeightKg, ' kg')}</span>}{vehicle.doors != null && <span>{vehicle.doors} portas</span>}{vehicle.seats != null && <span>{vehicle.seats} lugares</span>}</div>
          {vehicle.fuelChoices.length > 0 && <div className="enrichment-fuel-choices"><small>Escolha explicitamente o consumo a importar:</small>{vehicle.fuelChoices.map((choice, index) => <button type="button" key={`${vehicle.id}-${index}`} className="button tiny secondary" onClick={() => applyLocalData(vehicle, choice)}>{choice.label}: {showValue(choice.consumptionPer100)} {choice.unit || ''} · Aplicar</button>)}</div>}
          <span className="enrichment-source">Confira a versão/ano antes de aplicar; campos não publicados continuam vazios. Fonte: {vehicle.sources.map((source) => source.name).join(', ') || vehicle.source}</span>
        </article>)}</div>}
      </div>
      <div className="form-grid two"><Field label="Carroceria" name="bodyClass" value={form.bodyClass} onChange={update} placeholder="Hatchback, SUV…" /><Field label="Combustível" name="fuelType" value={form.fuelType} onChange={update} placeholder="Elétrico, gasolina…" /><Field label="Tração" name="driveType" value={form.driveType} onChange={update} /><Field label="Câmbio" name="transmission" value={form.transmission} onChange={update} /><Field label="Portas" name="doors" value={form.doors} onChange={update} type="number" /><Field label="Lugares" name="seats" value={form.seats} onChange={update} type="number" /><Field label="Comprimento" name="lengthMm" value={form.lengthMm} onChange={update} type="number" suffix="mm" /><Field label="Largura" name="widthMm" value={form.widthMm} onChange={update} type="number" suffix="mm" /><Field label="Altura" name="heightMm" value={form.heightMm} onChange={update} type="number" suffix="mm" /><Field label="Entre-eixos" name="wheelbaseMm" value={form.wheelbaseMm} onChange={update} type="number" suffix="mm" /><Field label="Peso em ordem de marcha" name="curbWeightKg" value={form.curbWeightKg} onChange={update} type="number" suffix="kg" /><Field label="Bateria" name="batteryKwh" value={form.batteryKwh} onChange={update} type="number" suffix="kWh" /></div>
      {form.specsSource && <p className="source-note">Fonte técnica: <strong>{form.specsSource}</strong>. Campos sem retorno permanecem em branco.</p>}
    </section>

    <div className="drawer-actions"><div>{onDelete && <button type="button" className="button danger-text" onClick={onDelete}>Excluir carro</button>}</div><div className="action-group"><button type="button" className="button secondary" onClick={onCancel}>Cancelar</button><button type="submit" className="button primary" disabled={saving}>{saving ? 'Salvando…' : car ? 'Salvar alterações' : 'Salvar carro'}</button></div></div>
  </form>
}
