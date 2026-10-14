import { useEffect, useState } from 'react'
import AuthScreen from './components/AuthScreen'
import Catalog from './components/Catalog'
import CarForm from './components/CarForm'
import Comparison from './components/Comparison'
import Dashboard from './components/Dashboard'
import { clearToken, createCar, deleteCar, getCars, getMe, getToken, updateCar } from './lib/api'
import type { Car, CarInput, CatalogFuelChoice, CatalogVehicle, User } from './lib/types'

type Tab = 'dashboard' | 'compare' | 'catalog'
const tabFromPath = (): Tab => window.location.pathname === '/comparar' ? 'compare' : window.location.pathname === '/catalogo' ? 'catalog' : 'dashboard'

function App() {
  const [tab, setTab] = useState<Tab>(tabFromPath)
  const [user, setUser] = useState<User | null>(null)
  const [authChecking, setAuthChecking] = useState(Boolean(getToken()))
  const [cars, setCars] = useState<Car[]>([])
  const [loading, setLoading] = useState(true)
  const [apiOnline, setApiOnline] = useState<boolean | null>(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState<Car | null>(null)
  const [prefill, setPrefill] = useState<Partial<CarInput> | undefined>(undefined)

  const loadCars = async () => {
    setLoading(true)
    try { setCars(await getCars()); setError(''); setApiOnline(true) }
    catch (err) { setApiOnline(false); setError(err instanceof Error ? err.message : 'Não foi possível carregar os carros.') }
    finally { setLoading(false) }
  }

  useEffect(() => {
    const token = getToken()
    if (!token) { setLoading(false); return }
    getMe().then((currentUser) => { setUser(currentUser); return loadCars() })
      .catch(() => { clearToken(); setUser(null); setLoading(false) })
      .finally(() => setAuthChecking(false))
  }, [])
  useEffect(() => { const handlePop = () => setTab(tabFromPath()); window.addEventListener('popstate', handlePop); return () => window.removeEventListener('popstate', handlePop) }, [])
  useEffect(() => { if (!notice) return; const timeout = window.setTimeout(() => setNotice(''), 3600); return () => window.clearTimeout(timeout) }, [notice])

  const onAuthenticated = async (authenticatedUser: User) => { setUser(authenticatedUser); await loadCars() }
  const logout = () => { clearToken(); setUser(null); setCars([]); setFormOpen(false); setError(''); setNotice('') }
  const navigate = (next: Tab) => { const path = next === 'compare' ? '/comparar' : next === 'catalog' ? '/catalogo' : '/'; window.history.pushState({}, '', path); setTab(next) }
  const openNew = (prefillData?: Partial<CarInput>) => { setEditing(null); setPrefill(prefillData); setFormOpen(true) }
  const openEdit = (car: Car) => { setEditing(car); setPrefill(undefined); setFormOpen(true) }
  const save = async (payload: CarInput) => { if (editing) { await updateCar(editing.id, payload); setNotice('Alterações salvas com sucesso.') } else { await createCar(payload); setNotice('Carro adicionado à sua seleção.') } await loadCars(); setFormOpen(false) }
  const remove = async () => { if (!editing) return; if (!window.confirm(`Excluir “${editing.name}”?`)) return; await deleteCar(editing.id); await loadCars(); setFormOpen(false); setNotice('Carro removido da seleção.') }
  const useCatalogVehicle = (vehicle: CatalogVehicle, choice?: CatalogFuelChoice) => {
    const name = [vehicle.make, vehicle.model, vehicle.trim].filter(Boolean).join(' ').trim()
    const isVin = vehicle.id.startsWith('nhtsa-')
    openNew({
      name,
      brand: vehicle.make || undefined,
      model: vehicle.model || undefined,
      version: vehicle.trim || undefined,
      modelYear: vehicle.modelYear || undefined,
      vin: isVin ? vehicle.id.substring('nhtsa-'.length) : undefined,
      consumptionPer100: choice?.consumptionPer100 ?? vehicle.consumptionPer100 ?? undefined,
      fuelType: choice?.fuelType || vehicle.fuelType || undefined,
      bodyClass: vehicle.category || undefined,
      transmission: vehicle.transmission || undefined,
      driveType: vehicle.driveType || undefined,
      doors: vehicle.doors ?? undefined,
      seats: vehicle.seats ?? undefined,
      lengthMm: vehicle.lengthMm ?? undefined,
      widthMm: vehicle.widthMm ?? undefined,
      heightMm: vehicle.heightMm ?? undefined,
      wheelbaseMm: vehicle.wheelbaseMm ?? undefined,
      curbWeightKg: vehicle.curbWeightKg ?? undefined,
      batteryKwh: vehicle.batteryKwh ?? undefined,
      electrificationLevel: vehicle.powertrain || undefined,
      specsSource: vehicle.source,
    })
    const plugIn = /plug[ -]?in|phev/i.test(vehicle.powertrain || '')
    setNotice(plugIn
      ? 'Ficha importada. Para este híbrido plug-in, o simulador calcula apenas uma fonte de energia por vez; ele não soma uso elétrico e combustível. Informe o preço e valide o modo selecionado antes de salvar.'
      : 'Ficha importada. Informe preço de compra e valide o consumo/preço de energia antes de salvar.')
  }

  if (authChecking) return <div className="auth-loading">CustoDrive <span>Validando sua sessão…</span></div>
  if (!user) return <AuthScreen onAuthenticated={onAuthenticated} />

  return <div className="app-shell">
    <aside className="sidebar">
      <div className="brand"><span className="brand-mark"><i />CD</span><span>Custo<span>Drive</span></span></div>
      <div className="side-caption">DECISÕES SOBRE CARROS</div>
      <nav className="nav-list">
        <button className={tab === 'dashboard' ? 'active' : ''} onClick={() => navigate('dashboard')}><span className="nav-icon">⌂</span> Painel</button>
        <button className={tab === 'compare' ? 'active' : ''} onClick={() => navigate('compare')}><span className="nav-icon">◫</span> Comparar <span className="nav-count">{cars.length}</span></button>
        <button className={tab === 'catalog' ? 'active' : ''} onClick={() => navigate('catalog')}><span className="nav-icon">⌁</span> Catálogo gratuito</button>
      </nav>
      <div className="sidebar-bottom"><div className="side-note"><span className="api-dot" /><div><strong>Dados privados</strong><small>Sua conta. Sua seleção.</small></div></div><span className="version">CUSTODRIVE · v1.0</span></div>
    </aside>
    <main className="main-area">
      <header className="topbar"><div className="mobile-brand"><span className="brand-mark"><i />CD</span><strong>CustoDrive</strong></div><div className="breadcrumb"><span>Minha seleção</span><b>/</b><strong>{tab === 'dashboard' ? 'Painel' : tab === 'compare' ? 'Comparar modelos' : 'Catálogo de dados'}</strong></div><div className="topbar-status"><span className={`status-dot ${apiOnline === false ? 'offline' : ''}`} /> {apiOnline === false ? 'API indisponível' : 'API + PostgreSQL conectados'} <div className="user-menu"><span className="user-name">{user.name}</span><button onClick={logout}>Sair</button></div></div></header>
      {error && <div className="global-alert error">{error} <button onClick={loadCars}>Tentar novamente</button></div>}
      {notice && <div className="global-alert success">{notice}</div>}
      {tab === 'dashboard' && <Dashboard cars={cars} onNew={() => openNew()} onEdit={openEdit} loading={loading} />}
      {tab === 'compare' && <Comparison cars={cars} onAdd={() => openNew()} />}
      {tab === 'catalog' && <Catalog onUseVehicle={useCatalogVehicle} />}
    </main>
    {formOpen && <div className="drawer-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setFormOpen(false) }}><aside className="drawer"><CarForm car={editing} initialValues={prefill} onCancel={() => setFormOpen(false)} onSubmit={save} onDelete={editing ? remove : undefined} /></aside></div>}
  </div>
}

export default App
