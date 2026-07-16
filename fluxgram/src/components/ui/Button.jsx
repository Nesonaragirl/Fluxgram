import clsx from 'clsx'

export function Button({ children, onClick, variant = 'default', size = 'md', className, title, disabled }) {
  return (
    <button
      onClick={onClick}
      title={title}
      disabled={disabled}
      className={clsx(
        'inline-flex items-center justify-center gap-1.5 rounded-lg font-medium transition-all select-none',
        'disabled:opacity-40 disabled:pointer-events-none',
        {
          'bg-flux-accent text-white hover:bg-flux-accentHover': variant === 'primary',
          'bg-flux-panel text-flux-text hover:bg-flux-border': variant === 'default',
          'bg-transparent text-flux-muted hover:text-flux-text hover:bg-flux-panel': variant === 'ghost',
          'bg-flux-danger/20 text-flux-danger hover:bg-flux-danger/30': variant === 'danger',
          'bg-flux-edit/20 text-flux-edit hover:bg-flux-edit/30': variant === 'warning',
          'px-2 py-1 text-xs': size === 'xs',
          'px-2.5 py-1.5 text-xs': size === 'sm',
          'px-3 py-1.5 text-sm': size === 'md',
          'px-4 py-2 text-sm': size === 'lg',
        },
        className
      )}
    >
      {children}
    </button>
  )
}

export function IconButton({ icon: Icon, onClick, title, active, size = 'md', color, className, disabled }) {
  return (
    <button
      onClick={onClick}
      title={title}
      disabled={disabled}
      className={clsx(
        'inline-flex items-center justify-center rounded-lg transition-all select-none',
        'disabled:opacity-40 disabled:pointer-events-none',
        active ? 'bg-flux-accent/20 text-flux-accent' : 'text-flux-muted hover:text-flux-text hover:bg-flux-panel',
        {
          'w-6 h-6': size === 'xs',
          'w-7 h-7': size === 'sm',
          'w-8 h-8': size === 'md',
          'w-9 h-9': size === 'lg',
        },
        className
      )}
      style={color && !active ? { color } : undefined}
    >
      <Icon size={size === 'xs' ? 12 : size === 'sm' ? 14 : size === 'md' ? 16 : 18} />
    </button>
  )
}
