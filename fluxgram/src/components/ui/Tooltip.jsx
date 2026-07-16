export function Tooltip({ children, text }) {
  if (!text) return children
  return (
    <div className="relative group">
      {children}
      <div className="
        absolute bottom-full left-1/2 -translate-x-1/2 mb-1.5 px-2 py-1
        bg-gray-900 text-white text-[11px] rounded whitespace-nowrap
        opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50
        border border-flux-border
      ">
        {text}
        <div className="absolute top-full left-1/2 -translate-x-1/2 border-4 border-transparent border-t-gray-900" />
      </div>
    </div>
  )
}
