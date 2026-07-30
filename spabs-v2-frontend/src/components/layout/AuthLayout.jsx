import logo from '../../assets/logo.png'

export default function AuthLayout({ title, subtitle, children }) {
  return (
    <div className="grid min-h-screen lg:grid-cols-2">
      <div className="bg-primary text-primary-content hidden flex-col justify-between p-12 lg:flex">
        <div className="flex items-center gap-3">
          <span className="text-2xl font-semibold tracking-wide">SPABS-V2</span>
        </div>
        <div>
          <h1 className="text-3xl font-semibold">Football Academy Management System</h1>
          <p className="mt-3 max-w-md text-sm opacity-80">
            Manage your football academy operations in one place.
          </p>
        </div>
        <span className="text-xs opacity-60">&copy; {new Date().getFullYear()} SPABS-V2</span>
      </div>

      <div className="flex items-center justify-center p-6">
        <div className="w-full max-w-sm">
          <div className="mb-6 flex flex-col items-center gap-2 text-center">
            <img src={logo} alt="" className="h-50 w-50" />
          </div>
          <h2 className="text-base-content text-2xl font-semibold">{title}</h2>
          {subtitle && <p className="text-base-content/60 mt-1 text-sm">{subtitle}</p>}
          <div className="mt-6">{children}</div>
        </div>
      </div>
    </div>
  )
}
