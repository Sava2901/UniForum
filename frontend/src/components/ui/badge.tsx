import * as React from "react"
import { cva, type VariantProps } from "class-variance-authority"

import { cn } from "@/lib/utils"

const badgeVariants = cva(
  "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2",
  {
    variants: {
      variant: {
        default:
          "border-transparent bg-slate-900 text-slate-50 hover:bg-slate-900/80 dark:bg-primary dark:text-primary-foreground dark:hover:bg-primary/80",
        secondary:
          "border-transparent bg-slate-100 text-slate-900 hover:bg-slate-100/80 dark:bg-secondary dark:text-secondary-foreground dark:hover:bg-secondary/80",
        destructive:
          "border-transparent bg-red-500 text-slate-50 hover:bg-red-500/80 dark:bg-destructive dark:text-destructive-foreground dark:hover:bg-destructive/80",
        outline: "text-slate-950 dark:text-foreground",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
)

export interface BadgeProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof badgeVariants> {}

function Badge({ className, variant, ...props }: BadgeProps) {
  return (
    <div className={cn(badgeVariants({ variant }), className)} {...props} />
  )
}

export { Badge, badgeVariants }