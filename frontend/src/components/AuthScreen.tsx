import { useState, type FormEvent } from 'react'
import { login, register, setToken } from '../lib/api'
import type { User } from '../lib/types'

type Props = { onAuthenticated: (user: User) => void }

export default function AuthScreen({ onAuthenticated }: Props) {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event: FormEvent) => {
    event.preventDefault()
    setBusy(true); setError('')
    try {
      const result = mode === 'register'
        ? await register(name, email, password)
        : await login(email, password)
      setToken(result.token)
      onAuthenticated(result.user)
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Não foi possível autenticar.')
    } finally {
      setBusy(false)
    }
  }

  return <main className="auth-page">
    <section className="auth-story">
      <div className="brand auth-brand"><span className="brand-mark"><i />CD</span><span>Custo<span>Drive</span></span></div>
      <div className="auth-story-copy"><span className="eyebrow">DECISÕES SOBRE CARROS</span><h1>Sua próxima escolha.<br /><em>Do seu jeito.</em></h1><p>Compare custos, salve opções e encontre o carro que faz sentido para você.</p><div className="auth-story-stat"><strong>R$</strong><span><b>Custos claros.</b><small>Sua seleção, só sua.</small></span></div></div>
      <div className="auth-story-footer">SEU ESPAÇO PESSOAL DE DECISÃO · CUSTODRIVE</div>
      <div className="auth-orbit orbit-one" /><div className="auth-orbit orbit-two" />
    </section>
    <section className="auth-panel"><div className="auth-mobile-brand"><span className="brand-mark"><i />CD</span><strong>CustoDrive</strong></div><div className="auth-card">
      <span className="eyebrow">{mode === 'login' ? 'BEM-VINDO DE VOLTA' : 'COMECE SUA SELEÇÃO'}</span>
      <h2>{mode === 'login' ? 'Entre na sua conta.' : 'Crie sua conta.'}</h2>
      <p className="auth-subtitle">{mode === 'login' ? 'Seus carros e comparações esperam por você.' : 'Sua lista de carros fica privada e associada à sua conta.'}</p>
      <div className="auth-tabs"><button type="button" className={mode === 'login' ? 'selected' : ''} onClick={() => { setMode('login'); setError('') }}>Entrar</button><button type="button" className={mode === 'register' ? 'selected' : ''} onClick={() => { setMode('register'); setError('') }}>Criar conta</button></div>
      <form onSubmit={submit} className="auth-form">
        {mode === 'register' && <label>Nome<input autoComplete="name" value={name} onChange={(event) => setName(event.target.value)} placeholder="Como podemos chamar você?" required maxLength={120} /></label>}
        <label>E-mail<input type="email" autoComplete="email" value={email} onChange={(event) => setEmail(event.target.value)} placeholder="voce@exemplo.com" required maxLength={254} /></label>
        <label>Senha<input type="password" autoComplete={mode === 'login' ? 'current-password' : 'new-password'} value={password} onChange={(event) => setPassword(event.target.value)} placeholder={mode === 'login' ? 'Sua senha' : 'Mínimo de 8 caracteres'} required minLength={8} maxLength={72} /></label>
        {error && <div className="auth-error">{error}</div>}
        <button className="button primary auth-submit" type="submit" disabled={busy}>{busy ? 'Aguarde…' : mode === 'login' ? 'Entrar na minha conta' : 'Criar conta'}</button>
      </form>
      <div className="auth-security"><span>▣</span> Seus dados de carros são privados e separados por conta.</div>
    </div><div className="auth-panel-footer">Não compartilhe sua senha. A autenticação usa senha protegida e token de sessão.</div></section>
  </main>
}
