export function DateRangePicker() {
  return (
    <div className="grid-2">
      <label className="field" htmlFor="dateFrom">
        <span>Başlanğıc</span>
        <input id="dateFrom" type="date" />
      </label>
      <label className="field" htmlFor="dateTo">
        <span>Son</span>
        <input id="dateTo" type="date" />
      </label>
    </div>
  );
}
