import type { FieldError } from "react-hook-form";

type FormFieldProps = {
  error?: FieldError;
  label: string;
  name: string;
} & React.InputHTMLAttributes<HTMLInputElement>;

export function FormField({ error, label, name, ...props }: FormFieldProps) {
  return (
    <label className="field" htmlFor={name}>
      <span>{label}</span>
      <input id={name} name={name} {...props} />
      {error?.message ? <span className="field-error">{error.message}</span> : null}
    </label>
  );
}
