"use client";

import { cloneElement, isValidElement } from "react";
import clsx from "clsx";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

type ButtonProps = React.ButtonHTMLAttributes<HTMLButtonElement> & {
  asChild?: boolean;
  iconOnly?: boolean;
  variant?: ButtonVariant;
};

export function Button({
  asChild,
  children,
  className,
  iconOnly,
  variant = "primary",
  ...props
}: ButtonProps) {
  const classes = clsx("btn", `btn-${variant}`, iconOnly && "icon-button", className);

  if (asChild && isValidElement<{ className?: string }>(children)) {
    return cloneElement(children, {
      className: clsx(classes, children.props.className),
    });
  }

  return (
    <button className={classes} type="button" {...props}>
      {children}
    </button>
  );
}
