import clsx from 'clsx'

export function Badge({ children, color = 'accent', className }) {
  return (
    <span className={clsx(
      'inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-semibold uppercase tracking-wide',
      {
        'bg-flux-accent/20 text-flux-accent': color === 'accent',
        'bg-flux-edit/20 text-flux-edit': color === 'edit',
        'bg-flux-danger/20 text-flux-danger': color === 'danger',
        'bg-flux-success/20 text-flux-success': color === 'success',
        'bg-purple-500/20 text-purple-400': color === 'plugin',
        'bg-flux-muted/20 text-flux-muted': color === 'muted',
      },
      className
    )}>
      {children}
    </span>
  )
}
